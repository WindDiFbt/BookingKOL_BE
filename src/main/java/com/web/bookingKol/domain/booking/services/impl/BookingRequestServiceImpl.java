package com.web.bookingKol.domain.booking.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.BookingSingleReqDTO;
import com.web.bookingKol.domain.booking.dtos.BookingSingleResDTO;
import com.web.bookingKol.domain.booking.mappers.BookingSingleResMapper;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
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
import com.web.bookingKol.domain.kol.repositories.KolAvailabilityRepository;
import com.web.bookingKol.domain.kol.repositories.KolProfileRepository;
import com.web.bookingKol.domain.kol.services.KolWorkTimeService;
import com.web.bookingKol.domain.payment.dtos.PaymentReqDTO;
import com.web.bookingKol.domain.payment.services.PaymentService;
import com.web.bookingKol.domain.payment.services.SePayService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
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
    private SePayService sePayService;
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

    @Transactional
    @Override
    public ApiResponse<PaymentReqDTO> createBookingSingleReq(UUID userId, BookingSingleReqDTO bookingRequestDTO, List<MultipartFile> attachedFiles) {
        // --- 1. Fetch main entities ---
        KolProfile kol = kolProfileRepository.findById(bookingRequestDTO.getKolId())
                .orElseThrow(() -> new EntityNotFoundException("Kol Id Not Found: " + bookingRequestDTO.getKolId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Id Not Found: " + userId));
        // --- 2. Delegate validation ---
        bookingValidationService.validateBookingRequest(bookingRequestDTO, kol);
        //Check hold slot
        if (!softHoldBookingService.checkAndReleaseSlot(kol.getId(),
                bookingRequestDTO.getStartAt(),
                bookingRequestDTO.getEndAt(),
                userId.toString())) {
            throw new IllegalArgumentException("The time slot is no longer held by you, please re-select!");
        }
        // --- 3. Create Booking Request ---
        BookingRequest newBookingRequest = new BookingRequest();
        UUID bookingRequestId = UUID.randomUUID();
        newBookingRequest.setId(bookingRequestId);
        newBookingRequest.setKol(kol);
        newBookingRequest.setUser(user);
        newBookingRequest.setDescription(bookingRequestDTO.getDescription());
        newBookingRequest.setLocation(bookingRequestDTO.getLocation());
        newBookingRequest.setStartAt(bookingRequestDTO.getStartAt());
        newBookingRequest.setEndAt(bookingRequestDTO.getEndAt());
        newBookingRequest.setStatus(Enums.BookingStatus.REQUESTED.name());
        newBookingRequest.setBookingType(Enums.BookingType.SINGLE.name());
        newBookingRequest.setCreatedAt(Instant.now());
        // --- 4. Handle File Attachments ---
        if (attachedFiles != null && !attachedFiles.isEmpty()) {
            for (MultipartFile file : attachedFiles) {
                File fileUploaded = fileService.getFileUploaded(userId, file);
                FileUsageDTO fileUsageDTO = fileService.createFileUsage(fileUploaded, bookingRequestId, Enums.TargetType.ATTACHMENTS.name(), false);
                newBookingRequest.getAttachedFiles().add(fileUsageMapper.toEntity(fileUsageDTO));
            }
        }
        // --- 5. Create Contract ---
        Contract contract = new Contract();
        bookingRequestRepository.saveAndFlush(newBookingRequest);
        if (bookingRequestDTO.getIsConfirmWithTerms() == true) {
            contract = contractService.createNewContract(newBookingRequest);
            newBookingRequest.getContracts().add(contract);
        }
        // --- 6. Initiate Payment ---
        String transferContent = contract.getId().toString();
        PaymentReqDTO paymentReqDTO = paymentService.initiatePayment(
                newBookingRequest,
                contract,
                sePayService.createQRCode(contract.getAmount(), transferContent),
                user,
                contract.getAmount()
        );
        paymentReqDTO.setTransferContent(transferContent);
        // --- Create KOL work time ---
        KolAvailability ka = kolAvailabilityRepository.findAvailability(kol.getId(), bookingRequestDTO.getStartAt().atZone(ZoneOffset.UTC).toOffsetDateTime(),
                bookingRequestDTO.getEndAt().atZone(ZoneOffset.UTC).toOffsetDateTime());
        kolWorkTimeService.createNewKolWorkTime(ka, newBookingRequest, Enums.BookingStatus.REQUESTED.name(),
                bookingRequestDTO.getStartAt(),
                bookingRequestDTO.getEndAt());
        // --- 7. Build and return response ---
        return ApiResponse.<PaymentReqDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Booking Request successfully!"))
                .data(paymentReqDTO)
                .build();
    }

    @Override
    public ApiResponse<List<BookingSingleResDTO>> getAllRequestAdmin(UUID kolId,
                                                                     String status,
                                                                     LocalDate startAt,
                                                                     LocalDate endAt,
                                                                     LocalDate createdAtFrom,
                                                                     LocalDate createdAtTo,
                                                                     int page,
                                                                     int size) {
        Specification<BookingRequest> spec = Specification.allOf();
        if (kolId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("kol").get("id"), kolId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
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

        List<BookingSingleResDTO> bookingSingleResDTOPage = bookingRequestRepository.findAll(spec, pageable)
                .map(bookingRequest -> bookingSingleResMapper.toDto(bookingRequest)).stream().toList();
        return ApiResponse.<List<BookingSingleResDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Booking Request successfully!"))
                .data(bookingSingleResDTOPage)
                .build();
    }

    @Override
    public ApiResponse<BookingSingleResDTO> getDetailBooking(UUID bookingRequestId) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(bookingRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Booking Request Not Found"));
        return ApiResponse.<BookingSingleResDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get detail booking request successfully!"))
                .data(bookingSingleResMapper.toDto(bookingRequest))
                .build();
    }

    public void acceptBookingRequest(BookingRequest bookingRequest) {
        bookingRequest.setStatus(Enums.BookingStatus.ACCEPTED.name());
        bookingRequestRepository.save(bookingRequest);
    }
}
