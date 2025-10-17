package com.web.bookingKol.domain.kol.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.UpdateEntityUtil;
import com.web.bookingKol.common.exception.UserAlreadyExistsException;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.file.FileValidator;
import com.web.bookingKol.domain.file.dtos.FileDTO;
import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import com.web.bookingKol.domain.file.mappers.FileMapper;
import com.web.bookingKol.domain.file.mappers.FileUsageMapper;
import com.web.bookingKol.domain.file.models.File;
import com.web.bookingKol.domain.file.models.FileUsage;
import com.web.bookingKol.domain.file.repositories.FileRepository;
import com.web.bookingKol.domain.file.repositories.FileUsageRepository;
import com.web.bookingKol.domain.file.services.FileService;
import com.web.bookingKol.domain.kol.dtos.*;
import com.web.bookingKol.domain.kol.mappers.FeedbackUserViewMapper;
import com.web.bookingKol.domain.kol.mappers.KolCreatedMapper;
import com.web.bookingKol.domain.kol.mappers.KolDetailMapper;
import com.web.bookingKol.domain.kol.mappers.KolProfileMapper;
import com.web.bookingKol.domain.kol.models.Category;
import com.web.bookingKol.domain.kol.models.KolFeedback;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.repositories.CategoryRepository;
import com.web.bookingKol.domain.kol.repositories.KolProfileRepository;
import com.web.bookingKol.domain.kol.services.KolProfileService;
import com.web.bookingKol.domain.user.models.Role;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.RoleRepository;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KolProfileServiceImpl implements KolProfileService {
    @Autowired
    private KolProfileRepository kolProfileRepository;
    @Autowired
    private KolProfileMapper kolProfileMapper;
    @Autowired
    private KolDetailMapper kolDetailMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private KolCreatedMapper kolCreatedMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FileUsageRepository fileUsageRepository;
    @Autowired
    private FileUsageMapper fileUsageMapper;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileValidator fileValidator;
    @Autowired
    private FeedbackUserViewMapper feedbackUserViewMapper;

    @Override
    public ApiResponse<KolDetailDTO> getKolProfileByUserId(UUID userId) {
        KolProfile kolProfile = kolProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("KolProfile not found for userId: " + userId));
        KolDetailDTO kolDetailDTO = kolDetailMapper.toDto(kolProfile);
        if (kolProfile.getKolFeedbacks() != null) {
            kolDetailDTO.setFeedbacks(kolProfile.getKolFeedbacks().stream()
                    .filter(KolFeedback::getIsPublic)
                    .sorted(Comparator.comparing(KolFeedback::getCreatedAt).reversed())
                    .limit(3)
                    .map(feedbackUserViewMapper::toDto).collect(Collectors.toSet()));
        }
        return ApiResponse.<KolDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get kol profile success"))
                .data(kolDetailDTO)
                .build();
    }

    @Override
    public ApiResponse<KolDetailDTO> getKolProfileByKolId(UUID kolId) {
        KolProfile kolProfile = kolProfileRepository.findByKolId(kolId)
                .orElseThrow(() -> new EntityNotFoundException("KolProfile not found for kolId: " + kolId));
        KolDetailDTO kolDetailDTO = kolDetailMapper.toDto(kolProfile);
        if (kolProfile.getKolFeedbacks() != null) {
            kolDetailDTO.setFeedbacks(kolProfile.getKolFeedbacks().stream()
                    .filter(KolFeedback::getIsPublic)
                    .sorted(Comparator.comparing(KolFeedback::getCreatedAt).reversed())
                    .limit(3)
                    .map(feedbackUserViewMapper::toDto).collect(Collectors.toSet()));
        }
        return ApiResponse.<KolDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get kol profile by kolId success"))
                .data(kolDetailDTO)
                .build();
    }

    @Override
    public ApiResponse<Page<KolProfileDTO>> getAllKol(
            BigDecimal minBookingPrice,
            Boolean isAvailable,
            Double minRating,
            int page,
            int size,
            Enums.Roles role
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("overallRating").descending());
        Page<KolProfileDTO> kolDtos = kolProfileRepository.findAllFiltered(minBookingPrice, isAvailable, minRating, role, pageable)
                .map(kol -> {
                    KolProfileDTO dto = kolProfileMapper.toDto(kol);
                    dto.setFileUsageDtos(getActiveCoverFiles(dto.getFileUsageDtos()));
                    return dto;
                });
        return ApiResponse.<Page<KolProfileDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get all kol profiles success"))
                .data(kolDtos)
                .build();
    }

    @Override
    public ApiResponse<Page<KolProfileDTO>> getAllKolAvailable(
            Double minRating, UUID categoryId, BigDecimal minPrice, int page, int size, Enums.Roles role) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "overallRating"));
        Page<KolProfileDTO> kolProfilePage = kolProfileRepository
                .findAllKolAvailableWithFilter(Enums.UserStatus.ACTIVE.name(), minRating, categoryId, minPrice, role, pageable)
                .map(kol -> {
                    KolProfileDTO dto = kolProfileMapper.toDto(kol);
                    dto.setFileUsageDtos(getActiveCoverFiles(dto.getFileUsageDtos()));
                    return dto;
                });
        return ApiResponse.<Page<KolProfileDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get available KOL profiles success"))
                .data(kolProfilePage)
                .build();
    }

    /**
     * Retrieve all KOL profiles that belong to a specific category.
     * Each KOL profile will contain cover files that are active and marked as cover.
     *
     * @param categoryId the ID of the category to filter KOLs
     * @return ApiResponse containing the list of KOL profiles under the given category
     */
    @Override
    public ApiResponse<List<KolProfileDTO>> getAllKolProfilesByCategoryId(UUID categoryId) {
        List<KolProfileDTO> KolProfileDTOS = kolProfileRepository.findByCategoryId(categoryId)
                .stream().map(kol -> {
                    KolProfileDTO dto = kolProfileMapper.toDto(kol);
                    dto.setFileUsageDtos(getActiveCoverFiles(dto.getFileUsageDtos()));
                    return dto;
                }).toList();
        return ApiResponse.<List<KolProfileDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get all kol by Category success!"))
                .data(KolProfileDTOS)
                .build();
    }

    /**
     * Filter and return only active cover files from the given set of FileUsageDTOs.
     * A file is considered valid if:
     * - isCover = true
     * - file is not null
     * - file status = ACTIVE
     *
     * @param fileUsageDTOs the set of FileUsageDTOs to filter
     * @return a set of only active cover FileUsageDTOs, or empty set if none
     */
    private Set<FileUsageDTO> getActiveCoverFiles(Set<FileUsageDTO> fileUsageDTOs) {
        if (fileUsageDTOs == null) return Collections.emptySet();
        return fileUsageDTOs.stream()
                .filter(fu -> Boolean.TRUE.equals(fu.getIsCover())
                        && fu.getFile() != null
                        && Enums.FileStatus.ACTIVE.name().equals(fu.getFile().getStatus()))
                .collect(Collectors.toSet());
    }

    /**
     * Create a new KOL account along with their profile.
     * Only an admin user can perform this operation.
     * If an avatar file is provided, it will be uploaded and linked to the KOL profile.
     *
     * @param AdminId    the ID of the admin user creating the KOL
     * @param newKolDTO  the details of the new KOL to create
     * @param fileAvatar (optional) avatar image file for the KOL
     * @return ApiResponse containing the created KOL details
     * @throws AccessDeniedException      if the user is not an admin
     * @throws UserAlreadyExistsException if the email already exists
     * @throws IllegalArgumentException   if required roles or categories are not found
     */
    @Transactional
    @Override
    public ApiResponse<KolCreatedDTO> createNewKolAccount(UUID AdminId, NewKolDTO newKolDTO, MultipartFile fileAvatar) {
        User admin = userRepository.findById(AdminId)
                .orElseThrow(() -> new AccessDeniedException("Only admin can create new Kol accounts"));
        Role role = roleRepository.findByKey(Enums.Roles.KOL.name())
                .orElseThrow(() -> new IllegalArgumentException("Role KOL not found !!"));
        //Create user account for kol
        User newKol = new User();
        if (userRepository.existsByEmail(newKolDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + newKolDTO.getEmail());
        }
        newKol.setEmail(newKolDTO.getEmail());
        newKol.setPhone(newKolDTO.getPhone());
        newKol.setFullName(newKolDTO.getFullName());
        newKol.setPasswordHash(passwordEncoder.encode(newKolDTO.getPassword()));
        if (newKolDTO.getGender() != null) {
            newKol.setGender(newKolDTO.getGender().trim().equalsIgnoreCase(Enums.UserGender.Male.name()) ?
                    Enums.UserGender.Male.name() : Enums.UserGender.Female.name());
        }
        newKol.setRoles(new LinkedHashSet<>(Collections.singletonList(role)));
        newKol.setAddress(newKolDTO.getAddress());
        newKol.setStatus(Enums.UserStatus.ACTIVE.name());
        newKol.setCreatedAt(Instant.now());
        newKol = userRepository.saveAndFlush(newKol);
        //Create kol profile after user account is created
        KolProfile kolProfile = new KolProfile();
        kolProfile.setUser(newKol);
        kolProfile.setId(UUID.randomUUID());
        kolProfile.setDisplayName(newKolDTO.getDisplayName());
        kolProfile.setBio(newKolDTO.getBio());
        kolProfile.setCountry(newKolDTO.getCountry());
        kolProfile.setCity(newKolDTO.getCity());
        kolProfile.setLanguages(newKolDTO.getLanguages());
        kolProfile.setRateCardNote(newKolDTO.getRateCardNote());
        kolProfile.setMinBookingPrice(newKolDTO.getMinBookingPrice());
        kolProfile.setIsAvailable(true);
        kolProfile.setCreatedAt(Instant.now());
        kolProfile.setDob(newKolDTO.getDob());
        kolProfile.setExperience(newKolDTO.getExperience());
        if (newKolDTO.getRole().equals(Enums.Roles.LIVE)) {
            kolProfile.setRole(Enums.Roles.LIVE);
        }
        if (newKolDTO.getCategoryIds() != null && !newKolDTO.getCategoryIds().isEmpty()) {
            Set<Category> categories = newKolDTO.getCategoryIds().stream()
                    .map(catId -> categoryRepository.findById(catId)
                            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + catId)))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            kolProfile.setCategories(categories);
        }
        kolProfileRepository.save(kolProfile);
        if (fileAvatar != null && !fileAvatar.isEmpty()) {
            if (!fileValidator.isImage(fileAvatar)) {
                throw new IllegalArgumentException("File is not an image");
            }
            FileDTO fileDTO = fileService.uploadFilePoint(admin.getId(), fileAvatar);
            newKol.setAvatarUrl(fileDTO.getFileUrl());
            // Link avatar file to kol profile
            fileService.createFileUsage(fileMapper.toEntity(fileDTO), kolProfile.getId(), Enums.TargetType.AVATAR.name(), false);
            userRepository.save(newKol);
        }
        return ApiResponse.<KolCreatedDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message(List.of("Create new kol account successfully"))
                .data(kolCreatedMapper.toDto(kolProfile, kolProfile.getUser()))
                .build();
    }

    /**
     * Update an existing KOL profile and their associated user information.
     * Only an admin user can perform this operation.
     * If a new avatar file is provided, it will be uploaded and linked to the KOL profile.
     *
     * @param changerId    the ID of the admin/Kol performing the update
     * @param kolId        the ID of the KOL profile to update
     * @param updateKolDTO the updated details for the KOL
     * @return ApiResponse containing the updated KOL details
     * @throws AccessDeniedException   if the user is not an admin
     * @throws EntityNotFoundException if the KOL profile is not found
     */
    @Transactional
    @Override
    public ApiResponse<KolDetailDTO> updateKolProfile(UUID changerId, UUID kolId, UpdateKolDTO updateKolDTO) {
        KolProfile kolProfile = validateAndGetKolProfile(changerId, kolId);
        User kolUser = kolProfile.getUser();
        // Update kol user info and kol profile info
        if (updateKolDTO != null) {
            BeanUtils.copyProperties(updateKolDTO, kolUser, UpdateEntityUtil.getNullPropertyNames(updateKolDTO));
            BeanUtils.copyProperties(updateKolDTO, kolProfile, UpdateEntityUtil.getNullPropertyNames(updateKolDTO));
        }
        kolProfile.setUpdatedAt(Instant.now());
        userRepository.save(kolUser);
        kolProfileRepository.save(kolProfile);
        return ApiResponse.<KolDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Update kol profile successfully"))
                .data(kolDetailMapper.toDto(kolProfile))
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<List<FileUsageDTO>> uploadKolMedias(UUID uploaderId, UUID kolId, List<MultipartFile> files) {
        KolProfile kolProfile = validateAndGetKolProfile(uploaderId, kolId);
        if (files != null && !files.isEmpty()) {
            List<FileUsageDTO> fileUsageDTOS = new ArrayList<>();
            for (MultipartFile file : files) {
                FileDTO fileDTO = fileService.uploadFilePoint(uploaderId, file);
                FileUsageDTO fileUsageDTO = fileService.createFileUsage(fileMapper.toEntity(fileDTO), kolProfile.getId(), Enums.TargetType.PORTFOLIO.name(), false);
                fileUsageDTOS.add(fileUsageDTO);
            }
            return ApiResponse.<List<FileUsageDTO>>builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Upload media files success: " + fileUsageDTOS.size() + " files"))
                    .data(fileUsageDTOS)
                    .build();
        } else {
            return ApiResponse.<List<FileUsageDTO>>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("No files to upload"))
                    .data(null)
                    .build();
        }
    }

    @Override
    public ApiResponse<List<FileUsageDTO>> getAllKolMediaFiles(UUID kolId) {
        if (!kolProfileRepository.existsById(kolId)) {
            throw new EntityNotFoundException("KolProfile not found for Kol Id: " + kolId);
        }
        List<FileUsageDTO> kolPortfolios = fileUsageMapper.toDtoList(fileUsageRepository.findAllImageAndVideoActiveByKolId(kolId));
        if (kolPortfolios == null) {
            return ApiResponse.<List<FileUsageDTO>>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(List.of("No files found"))
                    .data(null)
                    .build();
        }
        return ApiResponse.<List<FileUsageDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get kol portfolios and avatars success: " + kolPortfolios.size() + " files"))
                .data(kolPortfolios)
                .build();
    }

    /**
     * Activate or deactivate KOL media files based on the provided file usage IDs.
     * Only files associated with the KOL's portfolio can be modified.
     *
     * @param kolId        the ID of the KOL whose media files are to be modified
     * @param fileUsageIds the list of file usage IDs to activate/deactivate
     * @param isActive     true to activate, false to deactivate
     * @return ApiResponse indicating the result of the operation
     * @throws EntityNotFoundException if the KOL profile or any file usage is not found
     */
    @Override
    public ApiResponse<?> activateOrDeactivateKolMediaFile(UUID kolId, List<UUID> fileUsageIds, boolean isActive) {
        if (!kolProfileRepository.existsById(kolId)) {
            throw new EntityNotFoundException("KolProfile not found for Kol Id: " + kolId);
        }
        List<FileUsage> fileUsages = fileUsageRepository.findAllById(fileUsageIds).stream()
                .filter(fu -> fu.getTargetType().equals(Enums.TargetType.PORTFOLIO.name())).toList();
        if (fileUsages.size() != fileUsageIds.size()) {
            throw new EntityNotFoundException("Some PORTFOLIO File not found for provided IDs" + fileUsages.size() + "/" + fileUsageIds.size());
        }
        fileUsages.forEach(fileUsage -> fileUsage.setIsActive(isActive));
        fileUsageRepository.saveAll(fileUsages);
        String action = isActive ? "activated" : "deactivated";
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Successfully " + action + " " + fileUsages.size() + " media files"))
                .data(null)
                .build();
    }

    @Override
    public ApiResponse<?> deleteFileMedia(UUID changerId, UUID fileId) {
        User changer = userRepository.findById(changerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + changerId));
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found for ID: " + fileId));
        boolean isUploader = file.getUploader().getId().equals(changer.getId());
        boolean isAdmin = changer.getRoles().stream()
                .anyMatch(role -> Enums.Roles.ADMIN.name().equals(role.getKey()));
        if (!isUploader && !isAdmin) {
            throw new AccessDeniedException("Only admin or the uploader can delete this file.");
        }
        file.setStatus(Enums.FileStatus.DELETED.name());
        fileRepository.save(file);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Delete media file successfully"))
                .data(null)
                .build();
    }

    @Override
    public ApiResponse<FileUsageDTO> setAvatarWithExistedImage(UUID kolId, UUID fileId) {
        KolProfile kolProfile = kolProfileRepository.findById(kolId)
                .orElseThrow(() -> new EntityNotFoundException("KolProfile not found for Kol Id: " + kolId));
        File newAvatar = fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found for ID: " + fileId));
        if (!newAvatar.getFileType().equals(Enums.FileType.IMAGE.name())) {
            throw new IllegalArgumentException("File is not an image for ID: " + fileId);
        }
        if (!newAvatar.getStatus().equals(Enums.FileStatus.ACTIVE.name())) {
            throw new IllegalArgumentException("File is not active for ID: " + fileId);
        }
        FileUsage newAvatarFU = newAvatar.getFileUsages().stream()
                .filter(fu -> fu.getTargetId().equals(kolId))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("File is not associated with this Kol"));
        kolProfile.getFileUsages().stream()
                .filter(fu -> fu.getIsActive() && fu.getTargetType().equals(Enums.TargetType.AVATAR.name()))
                .findAny()
                .ifPresent(fu -> {
                    fu.setIsActive(false);
                    fileUsageRepository.save(fu);
                });
        FileUsageDTO fileUsageDTO = null;
        if (newAvatarFU != null && newAvatarFU.getTargetType().equals(Enums.TargetType.AVATAR.name())) {
            if (!newAvatarFU.getIsActive()) {
                newAvatarFU.setIsActive(true);
                fileUsageRepository.save(newAvatarFU);
                fileUsageDTO = fileUsageMapper.toDto(newAvatarFU);
            }
        } else {
            fileUsageDTO = fileService.createFileUsage(newAvatar, kolId, Enums.TargetType.AVATAR.name(), false);
        }
        User user = kolProfile.getUser();
        user.setAvatarUrl(newAvatar.getFileUrl());
        userRepository.save(user);
        return ApiResponse.<FileUsageDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Set avatar successfully"))
                .data(fileUsageDTO)
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<FileUsageDTO> setAvatarWithUploadNewImage(UUID changerId, UUID kolId, MultipartFile fileAvatar) {
        if (fileAvatar.isEmpty() || fileAvatar.getSize() == 0) {
            throw new IllegalArgumentException("File is empty");
        }
        KolProfile kolProfile = validateAndGetKolProfile(changerId, kolId);
        kolProfile.getFileUsages().stream()
                .filter(fu -> fu.getIsActive() && fu.getTargetType().equals(Enums.TargetType.AVATAR.name()))
                .findAny()
                .ifPresent(fu -> {
                    fu.setIsActive(false);
                    fileUsageRepository.save(fu);
                });
        User kolUser = kolProfile.getUser();
        if (!fileValidator.isImage(fileAvatar)) {
            throw new IllegalArgumentException("File is not an image");
        }
        FileDTO fileDTO = fileService.uploadFilePoint(changerId, fileAvatar);
        kolUser.setAvatarUrl(fileDTO.getFileUrl());
        // Link avatar file to kol profile
        FileUsageDTO fileUsageDTO = fileService.createFileUsage(fileMapper.toEntity(fileDTO), kolProfile.getId(), Enums.TargetType.AVATAR.name(), false);
        userRepository.save(kolUser);
        return ApiResponse.<FileUsageDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Set avatar successfully"))
                .data(fileUsageDTO)
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<KolDetailDTO> addCategoryForKol(UUID changerId, UUID kolId, UUID categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("CategoryId cannot be null");
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));
        KolProfile kolProfile = validateAndGetKolProfile(changerId, kolId);
        kolProfile.getCategories().stream()
                .filter(ca -> ca.getId().equals(categoryId))
                .findAny()
                .ifPresent(ca -> {
                    throw new IllegalArgumentException("Category already exists for this kol: " + categoryId);
                });
        kolProfile.getCategories().add(category);
        kolProfileRepository.save(kolProfile);
        return ApiResponse.<KolDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Add category to kol successfully"))
                .data(kolDetailMapper.toDto(kolProfile))
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<KolDetailDTO> removeCategoryForKol(UUID changerId, UUID kolId, UUID categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("CategoryId cannot be null");
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found: " + categoryId);
        }
        KolProfile kolProfile = validateAndGetKolProfile(changerId, kolId);
        Category toRemove = kolProfile.getCategories().stream()
                .filter(ca -> ca.getId().equals(categoryId))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Category not found for this kol:" + categoryId));
        kolProfile.getCategories().remove(toRemove);
        kolProfileRepository.save(kolProfile);
        return ApiResponse.<KolDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Remove category from kol successfully"))
                .data(kolDetailMapper.toDto(kolProfile))
                .build();
    }

    private KolProfile validateAndGetKolProfile(UUID changerId, UUID kolId) {
        User changer = userRepository.findById(changerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + changerId));
        KolProfile kolProfile = kolProfileRepository.findById(kolId)
                .orElseThrow(() -> new EntityNotFoundException("Kol not found for Kol Id: " + kolId));
        boolean isAdmin = changer.getRoles().stream().anyMatch(role -> role.getKey().equals(Enums.Roles.ADMIN.name()));
        boolean isSelfKol = changer.getKolProfile() != null && changer.getKolProfile().getId().equals(kolId);
        if (!isAdmin && !isSelfKol) {
            throw new AccessDeniedException("Only admin or the kol himself can do this operation!");
        }
        return kolProfile;
    }

    @Override
    public KolProfile getKolProfileEntityByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found for Id: " + userId));
        return user.getKolProfile();
    }

    @Override
    public ApiResponse<FileUsageDTO> setCoverImage(UUID kolId, UUID fileId) {
        KolProfile kolProfile = kolProfileRepository.findById(kolId)
                .orElseThrow(() -> new EntityNotFoundException("KolProfile not found for Kol Id: " + kolId));
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found for ID: " + fileId));
        if (!file.getFileType().equals(Enums.FileType.IMAGE.name())) {
            throw new IllegalArgumentException("File is not an image for ID: " + fileId);
        }
        if (!file.getStatus().equals(Enums.FileStatus.ACTIVE.name())) {
            throw new IllegalArgumentException("File is not active for ID: " + fileId);
        }
        FileUsage fileUsage = file.getFileUsages().stream()
                .filter(fu -> fu.getTargetId().equals(kolId) && fu.getTargetType().equals(Enums.TargetType.PORTFOLIO.name()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("File is not associated with this Kol"));
        if (fileUsage.getTargetType().equals(Enums.TargetType.PORTFOLIO.name())) {
            kolProfile.getFileUsages().stream()
                    .filter(fu -> fu.getIsActive() && fu.getIsCover())
                    .findAny()
                    .ifPresent(fu -> {
                        fu.setIsCover(false);
                        fileUsageRepository.save(fu);
                    });
            fileUsage.setIsCover(true);
            fileUsageRepository.save(fileUsage);
        } else {
            throw new IllegalArgumentException("File is not a portfolio image of this Kol");
        }
        return ApiResponse.<FileUsageDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Set cover image successfully"))
                .data(fileUsageMapper.toDto(fileUsage))
                .build();
    }
}
