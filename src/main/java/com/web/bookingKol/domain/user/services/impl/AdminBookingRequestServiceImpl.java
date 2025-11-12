package com.web.bookingKol.domain.user.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.*;
import com.web.bookingKol.domain.booking.repositories.BookingRequestParticipantRepository;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.ContractPaymentScheduleRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.repositories.KolProfileRepository;
import com.web.bookingKol.domain.user.dtos.*;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.BookingPackageKolRepository;
import com.web.bookingKol.domain.user.repositories.CampaignRepository;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import com.web.bookingKol.domain.user.services.AdminBookingRequestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBookingRequestServiceImpl implements AdminBookingRequestService {

    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final ContractRepository contractRepository;
    private final ContractGeneratorService contractGeneratorService;
    private final BookingPackageKolRepository bookingPackageKolRepository;
    private final KolProfileRepository kolProfileRepository;
    private final BookingRequestParticipantRepository bookingRequestParticipantRepository;
    private final ContractPaymentScheduleRepository contractPaymentScheduleRepository;

    @Override
    @Transactional
    public ApiResponse<?> createBookingRequest(AdminCreateBookingRequestDTO dto, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin: " + adminEmail));

        Campaign campaign = campaignRepository.findById(dto.getCampaignId()).orElse(null);
        if (campaign == null) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Không tìm thấy campaign với ID: " + dto.getCampaignId()))
                    .data(null)
                    .build();
        }

        boolean exists = bookingRequestRepository.existsByCampaign_Id(campaign.getId());
        if (exists) {
            return ApiResponse.builder()
                    .status(HttpStatus.CONFLICT.value())
                    .message(List.of("Campaign này đã có Booking Request, không thể tạo thêm."))
                    .data(Map.of("campaignId", campaign.getId(), "campaignName", campaign.getName()))
                    .build();
        }


        BookingRequest booking = new BookingRequest();
        booking.setId(UUID.randomUUID());
        booking.setCampaign(campaign);
        booking.setUser(campaign.getCreatedBy());
        booking.setDescription(dto.getDescription());
        booking.setStatus(Enums.BookingStatus.REQUESTED.name());
        booking.setRepeatType(dto.getRepeatType());
        booking.setDayOfWeek(dto.getDayOfWeek());
        booking.setRepeatUntil(dto.getRepeatUntil());
        booking.setCreatedAt(Instant.now());
        booking.setUpdatedAt(Instant.now());
        booking.setContractAmount(dto.getContractAmount() != null ? dto.getContractAmount() : BigDecimal.ZERO);
        bookingRequestRepository.saveAndFlush(booking);

        String savedContractPath = null;
        MultipartFile contractFile = dto.getContractFile();

        Contract contract = new Contract();
        contract.setBookingRequest(booking);
        contract.setContractNumber("CT-" + System.currentTimeMillis());
        contract.setStatus(Enums.ContractStatus.DRAFT.name());
        contract.setCreatedAt(Instant.now());
        contract.setUpdatedAt(Instant.now());
        contract.setAmount(booking.getContractAmount());
        contractRepository.saveAndFlush(contract);

        try {
            if (contractFile != null && !contractFile.isEmpty()) {
                String uploadDir = "uploads/contracts/" + Instant.now().toEpochMilli();
                Files.createDirectories(Paths.get(uploadDir));

                String fileName = UUID.randomUUID() + "_" + contractFile.getOriginalFilename();
                Path filePath = Paths.get(uploadDir).resolve(fileName);
                Files.copy(contractFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                savedContractPath = filePath.toString();
            } else {
                var placeholders = Map.of(
                        "brand_name", campaign.getCreatedBy().getFullName(),
                        "kol_name", booking.getUser().getFullName(),
                        "campaign_name", campaign.getName(),
                        "today", LocalDate.now().toString(),
                        "contract_number", contract.getContractNumber(),
                        "contract_amount", booking.getContractAmount().toString()
                );

                var fileUsage = contractGeneratorService.generateAndSaveContract(placeholders, admin.getId(), contract.getId());
                savedContractPath = fileUsage.getFile().getFileUrl();
            }
        } catch (Exception e) {
            return ApiResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(List.of("Lỗi khi sinh hoặc upload hợp đồng: " + e.getMessage()))
                    .data(Map.of("campaignId", campaign.getId(), "bookingRequestId", booking.getId()))
                    .build();
        }


        contract.setTerms(savedContractPath != null
                ? "File hợp đồng: " + savedContractPath
                : "Chưa có file hợp đồng");
        contractRepository.save(contract);

        int inserted = 0;

        if (dto.getKolIds() != null && !dto.getKolIds().isEmpty()) {
            for (UUID kolId : dto.getKolIds()) {
                KolProfile kol = kolProfileRepository.findById(kolId)
                        .orElseThrow(() -> new RuntimeException("KOL không tồn tại: " + kolId));

                if (!bookingRequestParticipantRepository.existsByBookingRequest_IdAndKol_IdAndRole(
                        booking.getId(), kol.getId(), Enums.BookingParticipantRole.KOL)) {

                    BookingRequestParticipant p = new BookingRequestParticipant();
                    p.setBookingRequest(booking);
                    p.setKol(kol);
                    p.setRole(Enums.BookingParticipantRole.KOL);
                    p.setCreatedAt(Instant.now());
                    p.setUpdatedAt(Instant.now());
                    bookingRequestParticipantRepository.save(p);
                    inserted++;
                }
            }
        }


        if (dto.getLiveIds() != null && !dto.getLiveIds().isEmpty()) {
            for (UUID liveId : dto.getLiveIds()) {
                KolProfile live = kolProfileRepository.findById(liveId)
                        .orElseThrow(() -> new RuntimeException("Trợ LIVE không tồn tại: " + liveId));

                if (!bookingRequestParticipantRepository.existsByBookingRequest_IdAndKol_IdAndRole(
                        booking.getId(), live.getId(), Enums.BookingParticipantRole.LIVE)) {

                    BookingRequestParticipant p = new BookingRequestParticipant();
                    p.setBookingRequest(booking);
                    p.setKol(live);
                    p.setRole(Enums.BookingParticipantRole.LIVE);
                    p.setCreatedAt(Instant.now());
                    p.setUpdatedAt(Instant.now());
                    bookingRequestParticipantRepository.save(p);
                    inserted++;
                }
            }
        }

        booking.setStatus(Enums.BookingStatus.NEGOTIATING.name());
        booking.setUpdatedAt(Instant.now());
        bookingRequestRepository.save(booking);

        campaign.setStatus(Enums.BookingStatus.NEGOTIATING.name());
        campaignRepository.save(campaign);

        final String savedContractPathFinal = savedContractPath;
        final int participantCount = inserted;

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Tạo booking request thành công và chuyển trạng thái sang NEGOTIATING"))
                .data(new Object() {
                    public final UUID bookingRequestId = booking.getId();
                    public final String status = booking.getStatus();
                    public final String description = booking.getDescription();
                    public final String repeatType = booking.getRepeatType();
                    public final String dayOfWeek = booking.getDayOfWeek();
                    public final LocalDate repeatUntil = booking.getRepeatUntil();
                    public final BigDecimal contractAmount = booking.getContractAmount();
                    public final String campaignName = campaign.getName();
                    public final String createdBy = admin.getEmail();

                    public final UUID contractId = contract.getId();
                    public final String contractNumber = contract.getContractNumber();
                    public final String contractPath = savedContractPathFinal;
                    public final String contractStatus = contract.getStatus();

                    public final int participants = participantCount;
                    public final List<UUID> kolIds = dto.getKolIds();
                    public final List<UUID> liveIds = dto.getLiveIds();
                })
                .build();
    }




    @Override
    public ApiResponse<PagedResponse<AdminBookingRequestResponse>> getAllBookingRequests(Pageable pageable) {

        Page<BookingRequest> page = bookingRequestRepository.findByCampaignIsNotNull(pageable);

        Page<AdminBookingRequestResponse> mapped = page.map(br -> {
            var campaign = br.getCampaign();

            List<BookingRequestParticipant> participants =
                    bookingRequestParticipantRepository.findByBookingRequest_Id(br.getId());

            List<KolInfo> kols = participants.stream()
                    .filter(p -> p.getRole() == Enums.BookingParticipantRole.KOL)
                    .map(p -> KolInfo.builder()
                            .id(p.getKol().getId())
                            .displayName(p.getKol().getDisplayName())
                            .build())
                    .toList();

            List<KolInfo> lives = participants.stream()
                    .filter(p -> p.getRole() == Enums.BookingParticipantRole.LIVE)
                    .map(p -> KolInfo.builder()
                            .id(p.getKol().getId())
                            .displayName(p.getKol().getDisplayName())
                            .build())
                    .toList();

            Contract contract = br.getContracts().stream()
                    .max(Comparator.comparing(Contract::getCreatedAt))
                    .orElse(null);

            return AdminBookingRequestResponse.builder()
                    .bookingRequestId(br.getId())
                    .description(br.getDescription())
                    .status(br.getStatus())
                    .repeatType(br.getRepeatType())
                    .dayOfWeek(br.getDayOfWeek())
                    .repeatUntil(br.getRepeatUntil())
                    .contractAmount(br.getContractAmount())
                    .createdAt(br.getCreatedAt())
                    .updatedAt(br.getUpdatedAt())

                    .campaignId(campaign != null ? campaign.getId() : null)
                    .campaignName(campaign != null ? campaign.getName() : null)
                    .campaignObjective(campaign != null ? campaign.getObjective() : null)
                    .budgetMin(campaign != null ? campaign.getBudgetMin() : null)
                    .budgetMax(campaign != null ? campaign.getBudgetMax() : null)
                    .startDate(campaign != null ? campaign.getStartDate() : null)
                    .endDate(campaign != null ? campaign.getEndDate() : null)
                    .createdByEmail(campaign != null && campaign.getCreatedBy() != null
                            ? campaign.getCreatedBy().getEmail() : null)

                    .kols(kols)
                    .lives(lives)

                    .contractId(contract != null ? contract.getId() : null)
                    .contractNumber(contract != null ? contract.getContractNumber() : null)
                    .contractStatus(contract != null ? contract.getStatus() : null)
                    .contractTerms(contract != null ? contract.getTerms() : null)
                    .contractFileUrl(contract != null ? extractFileUrl(contract.getTerms()) : null)
                    .build();
        });

        return ApiResponse.<PagedResponse<AdminBookingRequestResponse>>builder()
                .status(200)
                .message(List.of("Lấy danh sách booking request thành công"))
                .data(PagedResponse.fromPage(mapped))
                .build();
    }


    private String extractFileUrl(String terms) {
        if (terms == null) return null;
        return terms.contains("uploads/") ? terms.substring(terms.indexOf("uploads/")).trim() : terms;
    }


    @Override
    @Transactional
    public ApiResponse<?> updateBookingRequestStatus(UUID id, UpdateBookingRequestStatusDTO dto, String adminEmail) {
        var admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin: " + adminEmail));

        var booking = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking request: " + id));

        booking.setStatus(dto.getStatus());
        booking.setUpdatedAt(java.time.Instant.now());
        bookingRequestRepository.save(booking);

        return ApiResponse.builder()
                .status(org.springframework.http.HttpStatus.OK.value())
                .message(java.util.List.of("Cập nhật trạng thái booking request thành công"))
                .data(new Object() {
                    public final java.util.UUID bookingRequestId = booking.getId();
                    public final String newStatus = booking.getStatus();
                    public final String updatedBy = admin.getEmail();
                })
                .build();
    }


    @Override
    public ApiResponse<AdminBookingRequestResponse> getBookingRequestDetail(UUID bookingRequestId) {
        BookingRequest br = bookingRequestRepository.findById(bookingRequestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking request: " + bookingRequestId));

        var campaign = br.getCampaign();

        List<BookingRequestParticipant> participants =
                bookingRequestParticipantRepository.findByBookingRequest_Id(br.getId());

        List<KolInfo> kols = participants.stream()
                .filter(p -> p.getRole() == Enums.BookingParticipantRole.KOL)
                .map(p -> KolInfo.builder()
                        .id(p.getKol().getId())
                        .displayName(p.getKol().getDisplayName())
                        .build())
                .toList();

        List<KolInfo> lives = participants.stream()
                .filter(p -> p.getRole() == Enums.BookingParticipantRole.LIVE)
                .map(p -> KolInfo.builder()
                        .id(p.getKol().getId())
                        .displayName(p.getKol().getDisplayName())
                        .build())
                .toList();

        Contract contract = br.getContracts().stream()
                .max(Comparator.comparing(Contract::getCreatedAt))
                .orElse(null);

        AdminBookingRequestResponse detail = AdminBookingRequestResponse.builder()
                .bookingRequestId(br.getId())
                .description(br.getDescription())
                .status(br.getStatus())
                .repeatType(br.getRepeatType())
                .dayOfWeek(br.getDayOfWeek())
                .repeatUntil(br.getRepeatUntil())
                .contractAmount(br.getContractAmount())
                .createdAt(br.getCreatedAt())
                .updatedAt(br.getUpdatedAt())

                .campaignId(campaign != null ? campaign.getId() : null)
                .campaignName(campaign != null ? campaign.getName() : null)
                .campaignObjective(campaign != null ? campaign.getObjective() : null)
                .budgetMin(campaign != null ? campaign.getBudgetMin() : null)
                .budgetMax(campaign != null ? campaign.getBudgetMax() : null)
                .startDate(campaign != null ? campaign.getStartDate() : null)
                .endDate(campaign != null ? campaign.getEndDate() : null)
                .createdByEmail(campaign != null && campaign.getCreatedBy() != null
                        ? campaign.getCreatedBy().getEmail() : null)

                .kols(kols)
                .lives(lives)

                .contractId(contract != null ? contract.getId() : null)
                .contractNumber(contract != null ? contract.getContractNumber() : null)
                .contractStatus(contract != null ? contract.getStatus() : null)
                .contractTerms(contract != null ? contract.getTerms() : null)
                .contractFileUrl(contract != null ? extractFileUrl(contract.getTerms()) : null)
                .build();

        return ApiResponse.<AdminBookingRequestResponse>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy chi tiết booking request thành công"))
                .data(detail)
                .build();
    }





    // xem chi tiết campaign
    @Override
    public ApiResponse<CampaignDetailResponse> getCampaignDetail(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy campaign với ID: " + campaignId));

        List<BookingRequest> bookingRequests = bookingRequestRepository.findByCampaign_Id(campaignId);

        List<BookingRequestDetail> bookingDetails = bookingRequests.stream().map(br -> {

            Contract contract = br.getContracts().stream()
                    .max(Comparator.comparing(Contract::getCreatedAt))
                    .orElse(null);

            List<PaymentScheduleResponse> paymentSchedules = contract != null
                    ? contractPaymentScheduleRepository.findByContract_Id(contract.getId())
                    .stream().map(sch -> PaymentScheduleResponse.builder()
                            .id(sch.getId())
                            .installmentNumber(sch.getInstallmentNumber())
                            .amount(sch.getAmount())

                            .dueDate(sch.getDueDate() != null
                                    ? sch.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
                                    : null)

                            .status(sch.getStatus().name())
                            .transactionId(sch.getTransaction() != null
                                    ? UUID.nameUUIDFromBytes(sch.getTransaction().getId().toString().getBytes())
                                    : null)

                            .transactionStatus(sch.getTransaction() != null ? sch.getTransaction().getStatus() : null)
                            .build())
                    .toList()
                    : List.of();


            // ====== Lấy danh sách KOL & LIVE ======
            List<BookingRequestParticipant> participants =
                    bookingRequestParticipantRepository.findByBookingRequest_Id(br.getId());

            List<KolInfo> kols = participants.stream()
                    .filter(p -> p.getRole() == Enums.BookingParticipantRole.KOL)
                    .map(p -> KolInfo.builder()
                            .id(p.getKol().getId())
                            .displayName(p.getKol().getDisplayName())
                            .build())
                    .toList();

            List<KolInfo> lives = participants.stream()
                    .filter(p -> p.getRole() == Enums.BookingParticipantRole.LIVE)
                    .map(p -> KolInfo.builder()
                            .id(p.getKol().getId())
                            .displayName(p.getKol().getDisplayName())
                            .build())
                    .toList();

            // ====== Trả về BookingRequest chi tiết ======
            return BookingRequestDetail.builder()
                    .id(br.getId())
                    .bookingNumber(br.getBookingNumber())
                    .status(br.getStatus())
                    .description(br.getDescription())
                    .repeatType(br.getRepeatType())
                    .dayOfWeek(br.getDayOfWeek())
                    .repeatUntil(br.getRepeatUntil())
                    .contractAmount(br.getContractAmount())
                    .createdAt(br.getCreatedAt())
                    .updatedAt(br.getUpdatedAt())

                    .campaignId(campaign.getId())
                    .campaignName(campaign.getName())
                    .campaignObjective(campaign.getObjective())
                    .budgetMin(campaign.getBudgetMin())
                    .budgetMax(campaign.getBudgetMax())
                    .startDate(campaign.getStartDate())
                    .endDate(campaign.getEndDate())
                    .createdByEmail(campaign.getCreatedBy() != null ? campaign.getCreatedBy().getEmail() : null)

                    .contractId(contract != null ? contract.getId() : null)
                    .contractNumber(contract != null ? contract.getContractNumber() : null)
                    .contractStatus(contract != null ? contract.getStatus() : null)
                    .contractReason(contract != null ? contract.getReason() : null)
                    .contractTerms(contract != null ? contract.getTerms() : null)
                    .contractFileUrl(contract != null ? extractFileUrl(contract.getTerms()) : null)
                    .signedAtBrand(contract != null ? contract.getSignedAtBrand() : null)
                    .signedAtKol(contract != null ? contract.getSignedAtKol() : null)
                    .amount(contract != null ? contract.getAmount() : null)

                    .paymentSchedules(paymentSchedules)

                    .kols(kols)
                    .lives(lives)
                    .build();
        }).toList();

        CampaignDetailResponse response = CampaignDetailResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .objective(campaign.getObjective())
                .budgetMin(campaign.getBudgetMin())
                .budgetMax(campaign.getBudgetMax())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .status(campaign.getStatus())
                .createdByEmail(campaign.getCreatedBy() != null ? campaign.getCreatedBy().getEmail() : null)
                .bookingRequests(bookingDetails)
                .build();

        return ApiResponse.<CampaignDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy chi tiết campaign thành công"))
                .data(response)
                .build();
    }






}

