package com.web.bookingKol.domain.course.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.NumberGenerateUtil;
import com.web.bookingKol.common.UpdateEntityUtil;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.course.CoursePackageRepository;
import com.web.bookingKol.domain.course.dtos.CoursePackageDTO;
import com.web.bookingKol.domain.course.dtos.CoursePaymentDTO;
import com.web.bookingKol.domain.course.dtos.UpdateCoursePackageDTO;
import com.web.bookingKol.domain.course.mappers.CoursePackageMapper;
import com.web.bookingKol.domain.course.mappers.CoursePaymentMapper;
import com.web.bookingKol.domain.course.models.CoursePackage;
import com.web.bookingKol.domain.course.models.PurchasedCoursePackage;
import com.web.bookingKol.domain.course.services.CoursePackageService;
import com.web.bookingKol.domain.file.dtos.FileDTO;
import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import com.web.bookingKol.domain.file.mappers.FileMapper;
import com.web.bookingKol.domain.file.mappers.FileUsageMapper;
import com.web.bookingKol.domain.file.models.File;
import com.web.bookingKol.domain.file.models.FileUsage;
import com.web.bookingKol.domain.file.repositories.FileRepository;
import com.web.bookingKol.domain.file.repositories.FileUsageRepository;
import com.web.bookingKol.domain.file.services.FileService;
import com.web.bookingKol.domain.payment.dtos.PaymentReqDTO;
import com.web.bookingKol.domain.payment.models.Payment;
import com.web.bookingKol.domain.payment.services.PaymentService;
import com.web.bookingKol.domain.payment.services.SePayService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.PurchasedCoursePackageRepository;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class CoursePackageServiceImpl implements CoursePackageService {
    @Autowired
    private CoursePackageRepository coursePackageRepository;
    @Autowired
    private CoursePackageMapper coursePackageMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileUsageMapper fileUsageMapper;
    @Autowired
    private FileUsageRepository fileUsageRepository;
    @Autowired
    private PurchasedCoursePackageRepository purchasedCoursePackageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CoursePaymentMapper coursePaymentMapper;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private SePayService sePayService;

    @Override
    public ApiResponse<CoursePackageDTO> getCoursePackageById(UUID coursePackageId) {
        CoursePackage coursePackage = coursePackageRepository.findByCoursePackageId(coursePackageId, Enums.TargetType.COURSE_PACKAGE.name())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Gói khóa học với id: " + coursePackageId));
        return ApiResponse.<CoursePackageDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy gói khóa học theo coursePackageId thành công"))
                .data(coursePackageMapper.toDto(coursePackage))
                .build();
    }

    @Override
    public ApiResponse<Page<CoursePackageDTO>> getAllCourse(
            Integer minPrice,
            Integer maxPrice,
            Integer minDiscount,
            Integer maxDiscount,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = Sort.by(sortBy);
        sort = sortDir.equalsIgnoreCase("desc") ? sort.descending() : sort.ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CoursePackageDTO> coursePackageDtos = coursePackageRepository
                .findAllAvailableForUser(minPrice, maxPrice, minDiscount, maxDiscount, pageable)
                .map(coursePackageMapper::toDto);
        return ApiResponse.<Page<CoursePackageDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy tất cả các gói khóa học có sẵn thành công"))
                .data(coursePackageDtos)
                .build();
    }

    @Override
    public ApiResponse<Page<CoursePackageDTO>> getAllCoursesAdmin(
            Boolean isAvailable,
            Integer minPrice,
            Integer maxPrice,
            Integer minDiscount,
            Integer maxDiscount,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = Sort.by(sortBy);
        sort = sortDir.equalsIgnoreCase("desc") ? sort.descending() : sort.ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CoursePackageDTO> coursePackageDtos = coursePackageRepository
                .findAllFiltered(isAvailable, minPrice, maxPrice, minDiscount, maxDiscount, pageable)
                .map(coursePackageMapper::toDto);
        return ApiResponse.<Page<CoursePackageDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy tất cả các gói khóa học thành công"))
                .data(coursePackageDtos)
                .build();
    }

    @Override
    public ApiResponse<CoursePackageDTO> getDetailCourseAdmin(UUID coursePackageId) {
        CoursePackage coursePackage = coursePackageRepository.findById(coursePackageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Gói khóa học với id: " + coursePackageId));
        return ApiResponse.<CoursePackageDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy gói khóa học theo coursePackageId thành công"))
                .data(coursePackageMapper.toDto(coursePackage))
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<CoursePackageDTO> createCoursePackage(UUID adminId, CoursePackageDTO coursePackageDTO, List<MultipartFile> courseMedias) {
        CoursePackage cp = new CoursePackage();
        UUID courseId = UUID.randomUUID();
        cp.setId(courseId);
        cp.setName(coursePackageDTO.getName());
        cp.setPrice(coursePackageDTO.getPrice());
        cp.setDiscount(coursePackageDTO.getDiscount());
        cp.setCurrentPrice(coursePackageDTO.getPrice() * (100 - coursePackageDTO.getDiscount()) / 100);
        cp.setDescription(coursePackageDTO.getDescription());
        cp.setIsAvailable(true);
        Set<FileUsage> imageFiles = new LinkedHashSet<>();
        if (courseMedias != null && !courseMedias.isEmpty()) {
            courseMedias.forEach(image -> {
                FileDTO fileDTO = fileService.uploadFilePoint(adminId, image);
                FileUsageDTO fileUsageDTO = fileService.createFileUsage(fileMapper.toEntity(fileDTO), courseId, Enums.TargetType.COURSE_PACKAGE.name(), false);
                imageFiles.add(fileUsageMapper.toEntity(fileUsageDTO));
            });
            cp.setFileUsages(imageFiles);
        }
        coursePackageRepository.save(cp);
        return ApiResponse.<CoursePackageDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Tạo gói khóa học thành công!"))
                .data(coursePackageMapper.toDto(cp))
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<CoursePackageDTO> updateCoursePackage(UUID coursePackageId, UpdateCoursePackageDTO updateCoursePackageDTO) {
        CoursePackage cp = coursePackageRepository.findById(coursePackageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Gói khóa học với id: " + coursePackageId));
        if (updateCoursePackageDTO != null) {
            BeanUtils.copyProperties(updateCoursePackageDTO, cp, UpdateEntityUtil.getNullPropertyNames(updateCoursePackageDTO));
            if (updateCoursePackageDTO.getPrice() != null || updateCoursePackageDTO.getDiscount() != null) {
                long price = cp.getPrice();
                int discount = cp.getDiscount();
                cp.setCurrentPrice(price * (100 - discount) / 100);
            }
        }
        coursePackageRepository.saveAndFlush(cp);
        return ApiResponse.<CoursePackageDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Cập nhật gói khóa học thành công!"))
                .data(coursePackageMapper.toDto(cp))
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<List<FileUsageDTO>> uploadCourseMediaFiles(UUID uploaderId, UUID courseId, List<MultipartFile> files) {
        CoursePackage cp = coursePackageRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Gói khóa học với id: " + courseId));
        if (files != null && !files.isEmpty()) {
            List<FileUsageDTO> fileUsageDTOS = new ArrayList<>();
            for (MultipartFile file : files) {
                FileDTO fileDTO = fileService.uploadFilePoint(uploaderId, file);
                FileUsageDTO fileUsageDTO = fileService.createFileUsage(fileMapper.toEntity(fileDTO), cp.getId(), Enums.TargetType.COURSE_PACKAGE.name(), false);
                fileUsageDTOS.add(fileUsageDTO);
            }
            return ApiResponse.<List<FileUsageDTO>>builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Tải lên tệp phương tiện thành công: " + fileUsageDTOS.size() + " tệp"))
                    .data(fileUsageDTOS)
                    .build();
        } else {
            return ApiResponse.<List<FileUsageDTO>>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Không có tệp nào để tải lên"))
                    .data(null)
                    .build();
        }

    }

    @Override
    public ApiResponse<?> removeCourseMediaFile(UUID courseId, List<UUID> fileUsageIds) {
        if (!coursePackageRepository.existsById(courseId)) {
            throw new EntityNotFoundException("Không tìm thấy Gói khóa học với id: " + courseId);
        }
        List<FileUsage> fileUsages = fileUsageRepository.findAllById(fileUsageIds).stream()
                .filter(fu -> fu.getTargetType().equals(Enums.TargetType.COURSE_PACKAGE.name())).toList();
        if (fileUsages.size() != fileUsageIds.size()) {
            throw new EntityNotFoundException("Một số tệp COURSE_PACKAGE không tìm thấy cho các ID được cung cấp " + fileUsages.size() + "/" + fileUsageIds.size());
        }
        fileUsages.forEach(fileUsage -> {
            fileUsage.setIsActive(false);
            fileUsage.setIsCover(false);
        });
        fileUsageRepository.saveAll(fileUsages);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xóa thành công " + fileUsages.size() + " tệp phương tiện"))
                .data(null)
                .build();
    }

    @Override
    public ApiResponse<FileUsageDTO> setCoverImage(UUID courseId, UUID fileId) {
        CoursePackage cp = coursePackageRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Gói khóa học với id: " + courseId));
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tệp với id: " + fileId));
        if (!file.getFileType().equals(Enums.FileType.IMAGE.name())) {
            throw new IllegalArgumentException("Tệp không phải là hình ảnh cho ID: " + fileId);
        }
        if (!file.getStatus().equals(Enums.FileStatus.ACTIVE.name())) {
            throw new IllegalArgumentException("Tệp không hoạt động cho ID: " + fileId);
        }
        FileUsage fileUsage = file.getFileUsages().stream()
                .filter(fu -> fu.getTargetId().equals(courseId) && fu.getTargetType().equals(Enums.TargetType.COURSE_PACKAGE.name()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Tệp không được liên kết với Id gói khóa học này: " + courseId));
        if (fileUsage.getTargetType().equals(Enums.TargetType.COURSE_PACKAGE.name())) {
            cp.getFileUsages().stream()
                    .filter(fu -> fu.getIsActive() && fu.getIsCover())
                    .findAny()
                    .ifPresent(fu -> {
                        fu.setIsCover(false);
                        fileUsageRepository.save(fu);
                    });
            fileUsage.setIsCover(true);
            fileUsageRepository.save(fileUsage);
        } else {
            throw new IllegalArgumentException("Tệp không phải là phương tiện COURSE_PACKAGE của gói Khóa học này!");
        }
        return ApiResponse.<FileUsageDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Đặt ảnh bìa thành công"))
                .data(fileUsageMapper.toDto(fileUsage))
                .build();
    }

    @Override
    public ApiResponse<?> deleteCoursePackage(UUID courseId) {
        CoursePackage cp = coursePackageRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Gói khóa học với id: " + courseId));
        if (cp.getIsAvailable() != false) {
            throw new IllegalArgumentException("Khóa học cần phải được ẩn trước khi xóa hoàn toàn.");
        }
        if (purchasedCoursePackageRepository.existsPurchasedCoursePackageByCoursePackageId(courseId)) {
            throw new IllegalArgumentException("Khóa học đã được mua, không thể xóa vĩnh viễn, courseId: " + courseId);
        }
        Set<FileUsage> fileUsages = cp.getFileUsages();
        for (FileUsage fileUsage : fileUsages) {
            fileUsageRepository.delete(fileUsage);
            fileRepository.delete(fileUsage.getFile());
        }
        coursePackageRepository.delete(cp);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xóa khóa học thành công" + courseId))
                .data(null)
                .build();
    }

    @Override
    public ApiResponse<CoursePaymentDTO> purchaseCoursePackage(UUID userId, UUID coursePackageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng id: " + userId));
        CoursePackage coursePackage = coursePackageRepository.findById(coursePackageId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói khóa học: " + coursePackageId));
        if (Boolean.FALSE.equals(coursePackage.getIsAvailable())) {
            throw new IllegalArgumentException("Gói khóa học không có sẵn!");
        }
        PurchasedCoursePackage purchased = new PurchasedCoursePackage();
        purchased.setId(UUID.randomUUID());
        purchased.setUser(user);
        purchased.setCoursePackage(coursePackage);
        purchased.setCurrentPrice(coursePackage.getCurrentPrice());
        purchased.setStatus(Enums.PurchasedCourse.NOTASSIGNED.name());
        purchased.setIsPaid(false);
        purchased.setEmail(user.getEmail());
        purchased.setStartDate(Instant.now());
        String purchasedCourseNumber;
        do {
            purchasedCourseNumber = NumberGenerateUtil.generateSecureRandomPurchasedCourseNumber();
        } while (purchasedCoursePackageRepository.existsByPurchasedCourseNumber(purchasedCourseNumber));
        purchased.setPurchasedCourseNumber(purchasedCourseNumber);
        purchasedCoursePackageRepository.save(purchased);
        return ApiResponse.<CoursePaymentDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Tạo yêu cầu mua khóa học thành công"))
                .data(coursePaymentMapper.toDto(purchased))
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<PaymentReqDTO> confirmPurchaseCoursePackage(UUID userId, UUID purchasedCoursePackageId) {
        PurchasedCoursePackage purchasedCoursePackage = purchasedCoursePackageRepository.findById(purchasedCoursePackageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu mua khóa học"));
        if (!purchasedCoursePackage.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xác nhận yêu cầu mua khóa học này");
        }
        String transferContent = purchasedCoursePackage.getId().toString();
        Payment payment = paymentService.initiateCoursePayment(
                purchasedCoursePackage,
                purchasedCoursePackage.getUser(),
                purchasedCoursePackage.getCurrentPrice()
        );
        PaymentReqDTO paymentReqDTO = paymentService.createCoursePaymentRequest(
                purchasedCoursePackage,
                payment,
                sePayService.createQRCode(BigDecimal.valueOf(purchasedCoursePackage.getCurrentPrice()), transferContent)
        );
        purchasedCoursePackage.setPayment(payment);
        purchasedCoursePackageRepository.save(purchasedCoursePackage);
        paymentReqDTO.setTransferContent(transferContent);
        return ApiResponse.<PaymentReqDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xác nhận yêu cầu mua khóa học thành công"))
                .data(paymentReqDTO)
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<CoursePaymentDTO> cancelPurchaseCoursePackage(UUID userId, UUID purchasedCoursePackageId) {
        PurchasedCoursePackage purchasedCoursePackage = purchasedCoursePackageRepository.findById(purchasedCoursePackageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu mua khóa học"));
        if (!purchasedCoursePackage.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Bạn không có quyền xác nhận yêu cầu mua khóa học này");
        }
        purchasedCoursePackageRepository.delete(purchasedCoursePackage);
        return ApiResponse.<CoursePaymentDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Hủy yêu cầu mua khóa học thành công"))
                .data(coursePaymentMapper.toDto(purchasedCoursePackage))
                .build();
    }
}
