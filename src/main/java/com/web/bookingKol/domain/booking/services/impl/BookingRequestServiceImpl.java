package com.web.bookingKol.domain.booking.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.NumberGenerateUtil;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.*;
import com.web.bookingKol.domain.booking.jobrunr.BookingRequestJob;
import com.web.bookingKol.domain.booking.jobrunr.WorkTimeJob;
import com.web.bookingKol.domain.booking.mappers.BookingDetailMapper;
import com.web.bookingKol.domain.booking.mappers.BookingSingleResMapper;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.BookingRequestParticipant;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestParticipantRepository;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.booking.services.BookingRequestService;
import com.web.bookingKol.domain.booking.services.BookingValidationService;
import com.web.bookingKol.domain.booking.services.ContractService;
import com.web.bookingKol.domain.booking.services.SoftHoldBookingService;
import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import com.web.bookingKol.domain.file.mappers.FileUsageMapper;
import com.web.bookingKol.domain.file.models.File;
import com.web.bookingKol.domain.file.services.FileService;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.repositories.KolAvailabilityRepository;
import com.web.bookingKol.domain.kol.repositories.KolProfileRepository;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import com.web.bookingKol.domain.kol.services.KolWorkTimeService;
import com.web.bookingKol.domain.payment.dtos.PaymentReqDTO;
import com.web.bookingKol.domain.payment.dtos.refund.RefundDTO;
import com.web.bookingKol.domain.payment.models.Merchant;
import com.web.bookingKol.domain.payment.models.Payment;
import com.web.bookingKol.domain.payment.services.MerchantService;
import com.web.bookingKol.domain.payment.services.PaymentService;
import com.web.bookingKol.domain.payment.services.QRGenerateService;
import com.web.bookingKol.domain.payment.services.RefundService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingRequestServiceImpl implements BookingRequestService {
    @Autowired
    private BookingRequestRepository bookingRequestRepository;
    @Autowired
    private KolProfileRepository kolProfileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingSingleResMapper bookingSingleResMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileUsageMapper fileUsageMapper;
    @Autowired
    private ContractService contractService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private SoftHoldBookingService softHoldBookingService;
    @Autowired
    private BookingValidationService bookingValidationService;
    @Autowired
    private KolAvailabilityRepository kolAvailabilityRepository;
    @Autowired
    private KolWorkTimeService kolWorkTimeService;
    @Autowired
    private BookingDetailMapper bookingDetailMapper;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private BookingRequestJob bookingRequestJob;
    @Autowired
    protected WorkTimeJob workTimeJob;
    @Autowired
    private MerchantService merchantService;
    public static final String PAYMENT_TRANSFER_CONTENT_FORMAT = "Thanh toan cho ";
    @Autowired
    private RefundService refundService;
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;
    @Autowired
    private QRGenerateService qRGenerateService;
    @Autowired
    private BookingRequestParticipantRepository bookingRequestParticipantRepository;

    @Transactional
    @Override
    public ApiResponse<BookingDetailDTO> createBookingSingleReq(UUID userId, BookingSingleReqDTO bookingRequestDTO, List<MultipartFile> attachedFiles) {
        // --- 1. Fetch main entities ---
        KolProfile kol = kolProfileRepository.findById(bookingRequestDTO.getKolId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Kol với ID: " + bookingRequestDTO.getKolId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        // --- 2. Delegate validation ---
        bookingValidationService.validateBookingRequest(bookingRequestDTO, kol);
        //Check hold slot
        if (!softHoldBookingService.checkAndReleaseSlot(kol.getId(),
                bookingRequestDTO.getStartAt(),
                bookingRequestDTO.getEndAt(),
                userId.toString())) {
            throw new IllegalArgumentException("Khung thời gian này không được bạn giữ, vui lòng chọn lại!");
        }
        // --- 3. Create Booking Request ---
        BookingRequest newBookingRequest = new BookingRequest();
        String requestNumber;
        do {
            requestNumber = NumberGenerateUtil.generateSecureRandomRequestNumber();
        } while (bookingRequestRepository.existsByRequestNumber(requestNumber));
        UUID bookingRequestId = UUID.randomUUID();
        newBookingRequest.setId(bookingRequestId);
        newBookingRequest.setRequestNumber(requestNumber);
        newBookingRequest.setKol(kol);
        newBookingRequest.setUser(user);
        newBookingRequest.setDescription(bookingRequestDTO.getDescription());
        newBookingRequest.setLocation(bookingRequestDTO.getLocation());
        newBookingRequest.setStartAt(bookingRequestDTO.getStartAt());
        newBookingRequest.setEndAt(bookingRequestDTO.getEndAt());
        newBookingRequest.setStatus(Enums.BookingStatus.DRAFT.name());
        newBookingRequest.setBookingType(Enums.BookingType.SINGLE.name());
        newBookingRequest.setCreatedAt(Instant.now());
        newBookingRequest.setFullName(bookingRequestDTO.getFullName());
        newBookingRequest.setEmail(bookingRequestDTO.getEmail());
        newBookingRequest.setPhone(bookingRequestDTO.getPhone());
        newBookingRequest.setPlatform(bookingRequestDTO.getPlatform());
        // --- 4. Handle File Attachments ---
        if (attachedFiles != null && !attachedFiles.isEmpty()) {
            for (MultipartFile file : attachedFiles) {
                File fileUploaded = fileService.getFileUploaded(userId, file);
                FileUsageDTO fileUsageDTO = fileService.createFileUsage(fileUploaded, bookingRequestId, Enums.TargetType.ATTACHMENTS.name(), false);
                newBookingRequest.getAttachedFiles().add(fileUsageMapper.toEntity(fileUsageDTO));
            }
        }
        bookingRequestRepository.saveAndFlush(newBookingRequest);
        // --- 5. Create Contract ---
        Contract contract = contractService.createNewContract(newBookingRequest);
        newBookingRequest.getContracts().add(contract);
        // --- 6. Build Job schedule and return response ---
        BackgroundJob.schedule(
                bookingRequestId,
                Instant.now().plus(15, ChronoUnit.MINUTES),
                () -> bookingRequestJob.closeDraftRequest(newBookingRequest.getId())
        );
        return ApiResponse.<BookingDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Yêu cầu đặt lịch thành công!"))
                .data(bookingDetailMapper.toDto(newBookingRequest))
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<PaymentReqDTO> confirmBookingSingleReq(UUID bookingRequestId, UUID userId) {
        BookingRequest bookingRequest = getAndValidateDraftBooking(bookingRequestId, userId);
        Contract contract = getFirstContract(bookingRequest);
        bookingRequest.setStatus(Enums.BookingStatus.REQUESTED.name());
        bookingRequestRepository.saveAndFlush(bookingRequest);
        contractService.confirmContract(contract);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        // --- 1. Initiate Payment ---
        String transferContent = PAYMENT_TRANSFER_CONTENT_FORMAT + contract.getContractNumber() + " " + contract.getId().toString();
        PaymentReqDTO paymentReqDTO = paymentService.initiatePayment(
                bookingRequest,
                contract,
                qRGenerateService.createQRCode(contract.getAmount(), transferContent),
                user,
                contract.getAmount()
        );
        paymentReqDTO.setTransferContent(transferContent);
        // --- 2. Create KOL work time ---
        KolAvailability ka = kolAvailabilityRepository.findAvailability(bookingRequest.getKol().getId(),
                bookingRequest.getStartAt(), bookingRequest.getEndAt());
        if (ka == null) {
            throw new EntityNotFoundException("Không tìm thấy lịch khả dụng (KOL Availability) cho khung thời gian này.");
        }
        KolWorkTime kolWorkTime = kolWorkTimeService.createNewKolWorkTime(ka, bookingRequest, Enums.KOLWorkTimeStatus.REQUESTED.name(),
                bookingRequest.getStartAt(), bookingRequest.getEndAt());
        // --- 3. Build Job schedule and return response ---
        BackgroundJob.delete(bookingRequestId);
        BackgroundJob.schedule(
                kolWorkTime.getId(),
                kolWorkTime.getEndAt().plus(3, ChronoUnit.DAYS),
                () -> workTimeJob.autoCompleteWorkTime(kolWorkTime.getId())
        );
        return ApiResponse.<PaymentReqDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xác nhận yêu cầu đặt lịch thành công!"))
                .data(paymentReqDTO)
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<BookingDetailDTO> cancelBookingSingleReq(UUID bookingRequestId, UUID userId) {
        BookingRequest bookingRequest = getAndValidateDraftBooking(bookingRequestId, userId);
        Contract contract = getFirstContract(bookingRequest);
        bookingRequest.setStatus(Enums.BookingStatus.CANCELLED.name());
        contractService.cancelContract(contract);
        bookingRequestRepository.saveAndFlush(bookingRequest);
        BackgroundJob.delete(bookingRequestId);
        softHoldBookingService.releaseSlot(
                bookingRequest.getKol().getId(),
                bookingRequest.getStartAt(),
                bookingRequest.getEndAt()
        );
        return ApiResponse.<BookingDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Hủy yêu cầu đặt lịch thành công!"))
                .data(bookingDetailMapper.toDto(bookingRequest))
                .build();
    }

    private BookingRequest getAndValidateDraftBooking(UUID bookingRequestId, UUID userId) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(bookingRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu đặt lịch với ID: " + bookingRequestId));
        if (!bookingRequest.getStatus().equalsIgnoreCase(Enums.BookingStatus.DRAFT.name())) {
            throw new IllegalArgumentException("Yêu cầu đặt lịch không ở trạng thái DRAFT. Không thể hoàn thành hành động.");
        }
        if (!bookingRequest.getUser().getId().equals(userId)) {
            throw new AuthorizationServiceException("Bạn không được phép thực hiện hành động này đối với yêu cầu đặt lịch này");
        }
        return bookingRequest;
    }

    private Contract getFirstContract(BookingRequest bookingRequest) {
        return bookingRequest.getContracts().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hợp đồng với ID yêu cầu đặt lịch: " + bookingRequest.getId()));
    }

    @Override
    public ApiResponse<List<BookingSingleResDTO>> getAllSingleRequestAdmin(UUID kolId,
                                                                           UUID userId,
                                                                           String status,
                                                                           String requestNumber,
                                                                           LocalDate startAt,
                                                                           LocalDate endAt,
                                                                           LocalDate createdAtFrom,
                                                                           LocalDate createdAtTo,
                                                                           int page,
                                                                           int size) {
        List<BookingSingleResDTO> bookingSingleResDTOPage = findAllWithCondition(kolId, userId, status, requestNumber, startAt, endAt, createdAtFrom, createdAtTo, page, size);
        return ApiResponse.<List<BookingSingleResDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy tất cả yêu cầu đặt lịch thành công!"))
                .data(bookingSingleResDTOPage)
                .build();
    }

    private List<BookingSingleResDTO> findAllWithCondition(UUID kolId,
                                                           UUID userId,
                                                           String status,
                                                           String requestNumber,
                                                           LocalDate startAt,
                                                           LocalDate endAt,
                                                           LocalDate createdAtFrom,
                                                           LocalDate createdAtTo,
                                                           int page,
                                                           int size) {
        Specification<BookingRequest> spec = Specification.allOf();
        spec = spec.and(((root, query, cb) -> cb.equal(root.get("bookingType"), Enums.BookingType.SINGLE.name())));
        if (kolId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("kol").get("id"), kolId));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (requestNumber != null && !requestNumber.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("requestNumber")), "%" + requestNumber.toLowerCase() + "%"));
        }
        if (startAt != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("startAt"),
                            startAt.atStartOfDay(ZoneOffset.UTC).toInstant()));
        }
        if (endAt != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("endAt"),
                            endAt.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));
        }
        if (createdAtFrom != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"),
                            createdAtFrom.atStartOfDay(ZoneOffset.UTC).toInstant()));
        }
        if (createdAtTo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"),
                            createdAtTo.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return bookingRequestRepository.findAll(spec, pageable)
                .map(bookingRequest -> bookingSingleResMapper.toDto(bookingRequest)).stream().toList();
    }

    @Override
    public ApiResponse<BookingDetailDTO> getDetailSingleRequestAdmin(UUID bookingRequestId) {
        BookingRequest bookingRequest = bookingRequestRepository.findByIdWithAttachedFiles(bookingRequestId);
        if (bookingRequest == null) {
            throw new EntityNotFoundException("Không tìm thấy yêu cầu đặt lịch");
        }
        return ApiResponse.<BookingDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy chi tiết yêu cầu đặt lịch thành công!"))
                .data(bookingDetailMapper.toDto(bookingRequest))
                .build();
    }

    @Override
    public ApiResponse<List<BookingSingleResDTO>> getAllSingleRequestUser(UUID userId,
                                                                          String status,
                                                                          String requestNumber,
                                                                          LocalDate startAt,
                                                                          LocalDate endAt,
                                                                          LocalDate createdAtFrom,
                                                                          LocalDate createdAtTo,
                                                                          int page,
                                                                          int size) {
        List<BookingSingleResDTO> bookingSingleResList = findAllWithCondition(null, userId, status, requestNumber, startAt, endAt, createdAtFrom, createdAtTo, page, size);
        return ApiResponse.<List<BookingSingleResDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy tất cả yêu cầu đặt lịch thành công, userId: " + userId))
                .data(bookingSingleResList)
                .build();
    }

    @Override
    public ApiResponse<List<BookingSingleResDTO>> getAllSingleRequestKol(UUID kolId,
                                                                         String status,
                                                                         String requestNumber,
                                                                         LocalDate startAt,
                                                                         LocalDate endAt,
                                                                         LocalDate createdAtFrom,
                                                                         LocalDate createdAtTo,
                                                                         int page,
                                                                         int size) {
        List<BookingSingleResDTO> bookingSingleResList = findAllWithCondition(kolId, null, status, requestNumber, startAt, endAt, createdAtFrom, createdAtTo, page, size);
        return ApiResponse.<List<BookingSingleResDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy tất cả yêu cầu đặt lịch thành công, kolId: " + kolId))
                .data(bookingSingleResList)
                .build();
    }

    @Override
    public ApiResponse<BookingDetailDTO> getDetailSingleRequestKol(UUID bookingRequestId, UUID kolId) {
        BookingRequest bookingRequest = bookingRequestRepository.findByIdWithAttachedFiles(bookingRequestId);
        if (bookingRequest == null) {
            throw new EntityNotFoundException("Không tìm thấy yêu cầu đặt lịch");
        }
        if (!bookingRequest.getKol().getId().equals(kolId)) {
            throw new AuthorizationServiceException("Bạn không được phép xem yêu cầu đặt lịch này");
        }
        return ApiResponse.<BookingDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy chi tiết yêu cầu đặt lịch thành công!"))
                .data(bookingDetailMapper.toDto(bookingRequest))
                .build();
    }

    @Override
    public ApiResponse<BookingDetailDTO> getDetailSingleRequestUser(UUID bookingRequestId, UUID userId) {
        BookingRequest bookingRequest = bookingRequestRepository.findByIdWithAttachedFiles(bookingRequestId);
        if (bookingRequest == null) {
            throw new EntityNotFoundException("Không tìm thấy yêu cầu đặt lịch");
        }
        if (!bookingRequest.getUser().getId().equals(userId)) {
            throw new AuthorizationServiceException("Bạn không được phép xem yêu cầu đặt lịch này");
        }
        return ApiResponse.<BookingDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy chi tiết yêu cầu đặt lịch thành công!"))
                .data(bookingDetailMapper.toDto(bookingRequest))
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<BookingDetailDTO> updateBookingRequest(UUID userId, UUID bookingRequestId, UpdateBookingReqDTO updateBookingReqDTO, List<MultipartFile> attachedFiles, List<UUID> fileIdsToDelete) {
        BookingRequest bookingRequest = bookingRequestRepository.findByIdWithAttachedFiles(bookingRequestId);
        if (bookingRequest == null) {
            throw new EntityNotFoundException("Không tìm thấy yêu cầu đặt lịch: " + bookingRequestId);
        }
        if (!bookingRequest.getUser().getId().equals(userId)) {
            throw new AuthorizationServiceException("Bạn không được phép cập nhật yêu cầu đặt lịch này");
        }
        if (bookingRequest.getUpdatedAt() != null) {
            throw new IllegalArgumentException("Yêu cầu đặt lịch chỉ có thể được cập nhật một lần");
        }
        if (Instant.now().isAfter(bookingRequest.getStartAt().minus(24, ChronoUnit.HOURS))) {
            throw new IllegalArgumentException("Yêu cầu đặt lịch chỉ có thể được cập nhật trước 24 giờ");
        }
        if (updateBookingReqDTO != null) {
            if (updateBookingReqDTO.getFullName() != null) {
                bookingRequest.setFullName(updateBookingReqDTO.getFullName());
            }
            if (updateBookingReqDTO.getPhone() != null) {
                bookingRequest.setPhone(updateBookingReqDTO.getPhone());
            }
            if (updateBookingReqDTO.getEmail() != null) {
                bookingRequest.setEmail(updateBookingReqDTO.getEmail());
            }
            if (updateBookingReqDTO.getLocation() != null) {
                bookingRequest.setLocation(updateBookingReqDTO.getLocation());
            }
            if (updateBookingReqDTO.getDescription() != null) {
                bookingRequest.setDescription(updateBookingReqDTO.getDescription());
            }
            if (updateBookingReqDTO.getPlatform() != null) {
                bookingRequest.setPlatform(updateBookingReqDTO.getPlatform());
            }
        }
        if (attachedFiles != null && !attachedFiles.isEmpty()) {
            for (MultipartFile file : attachedFiles) {
                File fileUploaded = fileService.getFileUploaded(userId, file);
                FileUsageDTO fileUsageDTO = fileService.createFileUsage(fileUploaded, bookingRequestId, Enums.TargetType.ATTACHMENTS.name(), false);
                bookingRequest.getAttachedFiles().add(fileUsageMapper.toEntity(fileUsageDTO));
            }
        }
        if (fileIdsToDelete != null) {
            bookingRequest.getAttachedFiles().removeIf(fileUsage ->
                    fileIdsToDelete.contains(fileUsage.getFile().getId()));
            fileService.deleteFile(fileIdsToDelete);
        }
//        bookingRequest.setUpdatedAt(Instant.now());
        bookingRequestRepository.saveAndFlush(bookingRequest);
        return ApiResponse.<BookingDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Cập nhật yêu cầu đặt lịch thành công"))
                .data(bookingDetailMapper.toDto(bookingRequest))
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<?> cancelBookingRequest(UUID userId, UUID bookingRequestId, String bankNumber, String bankName, String reason, String ownerName) {
        BookingRequest bookingRequest = bookingRequestRepository.findByIdWithAttachedFiles(bookingRequestId);
        if (bookingRequest == null) {
            throw new EntityNotFoundException("Không tìm thấy yêu cầu đặt lịch: " + bookingRequestId);
        }
        if (bookingRequest.getStatus().equalsIgnoreCase(Enums.BookingStatus.CANCELLED.name())) {
            throw new IllegalArgumentException("Yêu cầu đặt lịch đã bị hủy");
        }
        if (!bookingRequest.getUser().getId().equals(userId)) {
            throw new AuthorizationServiceException("Bạn không được phép hủy yêu cầu đặt lịch này");
        }
        bookingRequest.setStatus(Enums.BookingStatus.CANCELLED.name());
//        bookingRequest.setUpdatedAt(Instant.now());
        bookingRequestRepository.saveAndFlush(bookingRequest);
        Set<KolWorkTime> kolWorkTimes = bookingRequest.getKolWorkTimes();
        for (KolWorkTime kolWorkTime : kolWorkTimes) {
            kolWorkTime.setStatus(Enums.KOLWorkTimeStatus.CANCELLED.name());
        }
        kolWorkTimeRepository.saveAll(kolWorkTimes);
        Contract contract = contractRepository.findByRequestId(bookingRequestId);
        RefundDTO refundDTO = refundService.createRefundRequest(contract, bankNumber, bankName, reason, ownerName);
        contract.setStatus(Enums.ContractStatus.WAIT_FOR_REFUND.name());
        contractRepository.save(contract);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Hủy yêu cầu đặt lịch thành công"))
                .data(refundDTO)
                .build();
    }

    @Override
    public ApiResponse<PaymentReqDTO> continueBookingRequestPayment(UUID bookingRequestId, UUID userId) {
        Merchant merchant = merchantService.getMerchantIsActive();
        BookingRequest bookingRequest = bookingRequestRepository.findById(bookingRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu đặt lịch với ID: " + bookingRequestId));
        if (!bookingRequest.getUser().getId().equals(userId)) {
            throw new AuthorizationServiceException("Bạn không được phép thực hiện hành động này đối với yêu cầu đặt lịch này");
        }
        if (bookingRequest.getStatus().equals(Enums.BookingStatus.DRAFT.name())) {
            throw new IllegalArgumentException("Yêu cầu đặt lịch chưa được xác nhận!");
        }
        Contract contract = bookingRequest.getContracts().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hợp đồng với ID yêu cầu đặt lịch: " + bookingRequest.getId()));
        Payment payment = contract.getPayment();
        if (!payment.getStatus().equals(Enums.PaymentStatus.PENDING.name())) {
            throw new IllegalArgumentException("Đã quá thời gian thực hiện thanh toán cho yêu cầu đặt lịch này!");
        }
        String transferContent = PAYMENT_TRANSFER_CONTENT_FORMAT + contract.getContractNumber() + " " + contract.getId().toString();
        String qrUrl = qRGenerateService.createQRCode(contract.getAmount(), transferContent);
        PaymentReqDTO paymentReqDTO = PaymentReqDTO.builder()
                .contractId(contract.getId())
                .amount(contract.getAmount())
                .qrUrl(qrUrl)
                .userId(bookingRequest.getUser().getId())
                .expiresAt(payment.getExpiresAt())
                .name(merchant.getName())
                .bank(merchant.getBank())
                .accountNumber(merchant.getAccountNumber())
                .build();
        return ApiResponse.<PaymentReqDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Tiếp tục thanh toán yêu cầu đặt lịch thành công!"))
                .data(paymentReqDTO)
                .build();
    }

    @Override
    public ByteArrayInputStream exportBookingDataToExcel(String type) throws IOException {
        List<BookingExportDTO> bookingExportDTOList = contractRepository.findAllForExport(type);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách Hợp đồng");

        // --- STYLE ---
        CellStyle headerCellStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Style cho các ô dữ liệu (Text)
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        dataCellStyle.setWrapText(true);

        // Style cho các ô dữ liệu (Date) - Dùng cho ngày tháng năm
        DataFormat dataFormat = workbook.createDataFormat();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.cloneStyleFrom(dataCellStyle);
        dateCellStyle.setDataFormat(dataFormat.getFormat("yyyy-MM-dd"));

        // Style cho các ô dữ liệu (DateTime) - Dùng cho Instant (timestamp)
        CellStyle dateTimeCellStyle = workbook.createCellStyle();
        dateTimeCellStyle.cloneStyleFrom(dataCellStyle);
        dateTimeCellStyle.setDataFormat(dataFormat.getFormat("yyyy-MM-dd HH:mm"));

        // Style cho các ô dữ liệu (Number/Price)
        CellStyle priceCellStyle = workbook.createCellStyle();
        priceCellStyle.cloneStyleFrom(dataCellStyle);
        priceCellStyle.setDataFormat(dataFormat.getFormat("#,##0"));
        priceCellStyle.setAlignment(HorizontalAlignment.RIGHT);

        // --- HEADER ---
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(30);
        String[] columns = {
                "Mã Hợp đồng", "Trạng thái HĐ", "Giá trị HĐ",
                "Mã Yêu cầu",
                "Tên KOL", "Tên người thuê", "Email người thuê", "SĐT người thuê",
                "Chiến dịch", "Loại Booking", "Nền tảng",
                "Thời gian bắt đầu (Booking)", "Thời gian kết thúc (Booking)"
        };

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }
        int rowIdx = 1;
        for (BookingExportDTO booking : bookingExportDTOList) {
            if (booking == null) {
                continue;
            }
            Row dataRow = sheet.createRow(rowIdx++);
            Cell cell;
            cell = dataRow.createCell(0);
            cell.setCellValue(booking.getContractNumber());
            cell.setCellStyle(dataCellStyle);

            // 1. Trạng thái HĐ
            cell = dataRow.createCell(1);
            cell.setCellValue(booking.getContractStatus());
            cell.setCellStyle(dataCellStyle);

            // 2. Giá trị HĐ (Quan trọng)
            cell = dataRow.createCell(2);
            if (booking.getContractAmount() != null) {
                cell.setCellValue(booking.getContractAmount().doubleValue());
            } else {
                cell.setCellValue(0);
            }
            cell.setCellStyle(priceCellStyle);

            // 4. Mã Yêu cầu
            cell = dataRow.createCell(3);
            cell.setCellValue(booking.getRequestNumber());
            cell.setCellStyle(dataCellStyle);

            // 6. Tên KOL (Quan trọng)
            cell = dataRow.createCell(4);
            String kol = booking.getKolName();
            String kolName;
            if (kol != null) {
                kolName = kol;
                cell.setCellValue(kolName);
            } else {
                List<BookingRequestParticipant> bk = bookingRequestParticipantRepository.findByBookingRequest_Id(booking.getBookingId());
                StringBuilder kolNameBuilder = new StringBuilder();
                for (BookingRequestParticipant p : bk) {
                    kolNameBuilder.append(p.getKol().getUser().getFullName()).append(", ");
                }
                kolName = kolNameBuilder.toString();
                cell.setCellValue(kolName);
            }
            cell.setCellStyle(dataCellStyle);

            // 7. Tên Brand/Client (Người tạo booking)
            cell = dataRow.createCell(5);
            cell.setCellValue(booking.getBrandName());
            cell.setCellStyle(dataCellStyle);

            // 8. Email Brand
            cell = dataRow.createCell(6);
            cell.setCellValue(booking.getBrandEmail());
            cell.setCellStyle(dataCellStyle);


            // 9. SĐT Brand
            cell = dataRow.createCell(7);
            cell.setCellValue(booking.getBrandPhone());
            cell.setCellStyle(dataCellStyle);

            // 10. Chiến dịch
            cell = dataRow.createCell(8);
            cell.setCellValue(booking.getCampaignName());
            cell.setCellStyle(dataCellStyle);

            // 11. Loại Booking
            cell = dataRow.createCell(9);
            cell.setCellValue(booking.getBookingType());
            cell.setCellStyle(dataCellStyle);

            // 12. Nền tảng
            cell = dataRow.createCell(10);
            cell.setCellValue(booking.getPlatform());
            cell.setCellStyle(dataCellStyle);

            // 13. Thời gian bắt đầu (Booking)
            cell = dataRow.createCell(11);
            if (booking.getStartAt() != null) {
                cell.setCellValue(Date.from(booking.getStartAt()));
            }
            cell.setCellStyle(dateTimeCellStyle);

            // 14. Thời gian kết thúc (Booking)
            cell = dataRow.createCell(12);
            if (booking.getEndAt() != null) {
                cell.setCellValue(Date.from(booking.getEndAt()));
            }
            cell.setCellStyle(dateTimeCellStyle);
        }
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
