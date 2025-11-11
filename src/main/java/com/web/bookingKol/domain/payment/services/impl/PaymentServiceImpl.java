package com.web.bookingKol.domain.payment.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.jobrunr.BookingRequestJob;
import com.web.bookingKol.domain.booking.jobrunr.ReminderEmailJob;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.models.ContractPaymentSchedule;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.ContractPaymentScheduleRepository;
import com.web.bookingKol.domain.booking.services.ContractService;
import com.web.bookingKol.domain.booking.services.SoftHoldBookingService;
import com.web.bookingKol.domain.course.models.PurchasedCoursePackage;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import com.web.bookingKol.domain.payment.dtos.CampaignPaymentDTO;
import com.web.bookingKol.domain.payment.dtos.PaymentReqDTO;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionDTO;
import com.web.bookingKol.domain.payment.jobrunr.PaymentJob;
import com.web.bookingKol.domain.payment.models.Merchant;
import com.web.bookingKol.domain.payment.models.Payment;
import com.web.bookingKol.domain.payment.repositories.PaymentRepository;
import com.web.bookingKol.domain.payment.services.MerchantService;
import com.web.bookingKol.domain.payment.services.PaymentService;
import com.web.bookingKol.domain.payment.services.QRGenerateService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.PurchasedCoursePackageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BookingRequestRepository bookingRequestRepository;
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private PaymentJob paymentJob;
    @Autowired
    private ReminderEmailJob reminderEmailJob;
    @Autowired
    private PurchasedCoursePackageRepository purchasedCoursePackageRepository;
    @Autowired
    private ContractService contractService;
    @Autowired
    private SoftHoldBookingService softHoldBookingService;

    private final String CURRENCY = "VND";
    private final Integer EXPIRES_TIME = 15;
    public static final String PAYMENT_TRANSFER_CONTENT_FORMAT = "Thanh toan cho ";

    @Autowired
    private BookingRequestJob bookingRequestJob;
    @Autowired
    private ContractPaymentScheduleRepository contractPaymentScheduleRepository;
    @Autowired
    private QRGenerateService qRGenerateService;

    @Override
    public PaymentReqDTO initiatePayment(BookingRequest bookingRequest, Contract contract, String qrUrl, User user, BigDecimal amount) {
        Merchant merchant = merchantService.getMerchantIsActive();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setContract(contract);
        payment.setUser(user);
        payment.setTotalAmount(amount);
        payment.setCurrency(CURRENCY);
        payment.setStatus(Enums.PaymentStatus.PENDING.name());
        payment.setPaidAmount(null);
        payment.setFailureReason(null);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(null);
        payment.setExpiresAt(Instant.now().plus(EXPIRES_TIME, ChronoUnit.MINUTES));
        paymentRepository.save(payment);
        BackgroundJob.schedule(
                payment.getId(),
                Instant.now().plus(EXPIRES_TIME, ChronoUnit.MINUTES),
                () -> paymentJob.expirePayment(payment.getId())
        );
        return PaymentReqDTO.builder()
                .contractId(contract.getId())
                .amount(contract.getAmount())
                .qrUrl(qrUrl)
                .userId(user.getId())
                .expiresAt(payment.getExpiresAt())
                .name(merchant.getName())
                .bank(merchant.getBank())
                .accountNumber(merchant.getAccountNumber())
                .build();
    }

    @Transactional
    @Override
    public void updatePaymentAfterTransactionSuccess(TransactionDTO transactionDTO) {
        Payment payment = paymentRepository.findById(transactionDTO.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Payment với ID: " + transactionDTO.getPaymentId()));

        BigDecimal currentPaidAmount = payment.getPaidAmount() == null ? BigDecimal.ZERO : payment.getPaidAmount();
        BigDecimal newPaidAmount = currentPaidAmount.add(transactionDTO.getAmountIn());
        payment.setPaidAmount(newPaidAmount);
        payment.setUpdatedAt(Instant.now());
        BigDecimal totalAmount = payment.getTotalAmount();
        int compare = newPaidAmount.compareTo(totalAmount);
        payment.setUpdatedAt(Instant.now());
        String status = switch (compare) {
            case -1 -> Enums.PaymentStatus.UNDERPAID.name();
            case 0 -> Enums.PaymentStatus.PAID.name();
            default -> Enums.PaymentStatus.OVERPAID.name();
        };
        if (status.equals(Enums.PaymentStatus.PAID.name()) || status.equals(Enums.PaymentStatus.OVERPAID.name())) {
            //Booking Request
            BookingRequest bookingRequest = payment.getContract().getBookingRequest();
            bookingRequest.setStatus(Enums.BookingStatus.PAID.name());
            bookingRequestRepository.save(bookingRequest);
            BackgroundJob.schedule(
                    bookingRequest.getStartAt(),
                    () -> bookingRequestJob.autoSetInProgressStatus(bookingRequest.getId())
            );
            //Contract
            Contract contract = bookingRequest.getContracts().stream().findFirst().orElse(null);
            contractService.paidContract(contract);
            //Kol worktime
            Set<KolWorkTime> kolWorkTime = bookingRequest.getKolWorkTimes();
            for (KolWorkTime workTime : kolWorkTime) {
                if (workTime.getStatus().equals(Enums.KOLWorkTimeStatus.REQUESTED.name())) {
                    workTime.setStatus(Enums.KOLWorkTimeStatus.IN_PROGRESS.name());
                    BackgroundJob.schedule(
                            workTime.getStartAt().minus(24, ChronoUnit.HOURS),
                            () -> reminderEmailJob.sendWorkStartReminder(workTime.getId())
                    );
                }
            }
            kolWorkTimeRepository.saveAll(kolWorkTime);
        }
        payment.setStatus(status);
        paymentRepository.save(payment);
    }

    @Override
    public void updateCoursePaymentAfterTransactionSuccess(TransactionDTO transactionDTO) {
        Payment payment = paymentRepository.findById(transactionDTO.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Payment với ID: " + transactionDTO.getPaymentId()));

        BigDecimal currentPaidAmount = payment.getPaidAmount() == null ? BigDecimal.ZERO : payment.getPaidAmount();
        BigDecimal newPaidAmount = currentPaidAmount.add(transactionDTO.getAmountIn());
        payment.setPaidAmount(newPaidAmount);
        payment.setUpdatedAt(Instant.now());
        BigDecimal totalAmount = payment.getTotalAmount();
        int compare = newPaidAmount.compareTo(totalAmount);
        payment.setUpdatedAt(Instant.now());
        String status = switch (compare) {
            case -1 -> Enums.PaymentStatus.UNDERPAID.name();
            case 0 -> Enums.PaymentStatus.PAID.name();
            default -> Enums.PaymentStatus.OVERPAID.name();
        };
        if (status.equals(Enums.PaymentStatus.PAID.name()) || status.equals(Enums.PaymentStatus.OVERPAID.name())) {
            PurchasedCoursePackage purchasedCoursePackage = purchasedCoursePackageRepository.findPurchasedCoursePackageByPaymentId(transactionDTO.getPaymentId());
            if (purchasedCoursePackage == null) {
                throw new IllegalArgumentException("Không tìm thấy PurchasedCoursePackage với Payment ID: " + transactionDTO.getPaymentId());
            }
            purchasedCoursePackage.setIsPaid(true);
            purchasedCoursePackageRepository.save(purchasedCoursePackage);
        }
        payment.setStatus(status);
        paymentRepository.save(payment);
    }

    @Override
    public boolean checkContractPaymentSuccess(UUID contractId) {
        Payment payment = paymentRepository.findByContractId(contractId);
        if (payment == null) {
            throw new IllegalArgumentException("Không tìm thấy Payment với Contract ID: " + contractId);
        }
        return payment.getStatus().equals(Enums.PaymentStatus.PAID.name()) || payment.getStatus().equals(Enums.PaymentStatus.OVERPAID.name());
    }

    @Override
    public boolean checkPurchasedCoursePackagePaymentSuccess(UUID purchasedCoursePackageId) {
        PurchasedCoursePackage purchasedCoursePackage = purchasedCoursePackageRepository.findById(purchasedCoursePackageId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy PurchasedCoursePackage với ID: " + purchasedCoursePackageId));
        return purchasedCoursePackage.getIsPaid();
    }

    @Override
    public Payment initiateCoursePayment(PurchasedCoursePackage purchasedCoursePackage, User user, Long currentPrice) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setContract(null);
        payment.setUser(user);
        payment.setTotalAmount(BigDecimal.valueOf(purchasedCoursePackage.getCurrentPrice()));
        payment.setCurrency(CURRENCY);
        payment.setStatus(Enums.PaymentStatus.PENDING.name());
        payment.setPaidAmount(null);
        payment.setFailureReason(null);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(null);
        payment.setExpiresAt(Instant.now().plus(EXPIRES_TIME, ChronoUnit.MINUTES));
        paymentRepository.saveAndFlush(payment);
        BackgroundJob.schedule(
                payment.getId(),
                Instant.now().plus(EXPIRES_TIME, ChronoUnit.MINUTES),
                () -> paymentJob.expireCoursePayment(payment.getId())
        );
        return payment;
    }

    @Override
    public PaymentReqDTO createCoursePaymentRequest(PurchasedCoursePackage purchasedCoursePackage, Payment payment, String qrUrl) {
        User user = payment.getUser();
        Merchant merchant = merchantService.getMerchantIsActive();
        return PaymentReqDTO.builder()
                .contractId(purchasedCoursePackage.getId())
                .amount(BigDecimal.valueOf(purchasedCoursePackage.getCurrentPrice()))
                .qrUrl(qrUrl)
                .userId(user.getId())
                .expiresAt(payment.getExpiresAt())
                .name(merchant.getName())
                .bank(merchant.getBank())
                .accountNumber(merchant.getAccountNumber())
                .build();
    }

    @Override
    public ApiResponse<?> cancelPaymentBookingRequest(UUID userId, UUID bookingRequestId) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(bookingRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu đặt lịch với ID: " + bookingRequestId));
        if (!bookingRequest.getUser().getId().equals(userId)) {
            throw new AuthorizationServiceException("Bạn không được phép thực hiện hành động này đối với yêu cầu đặt lịch này");
        }
        Contract contract = bookingRequest.getContracts().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hợp đồng với ID yêu cầu đặt lịch: " + bookingRequest.getId()));
        Payment payment = contract.getPayment();
        if (payment == null) {
            throw new EntityNotFoundException("Không tìm thấy thanh toán với ID hợp đồng: " + contract.getId());
        }
        if (!Enums.PaymentStatus.PENDING.name().equals(payment.getStatus())) {
            throw new IllegalArgumentException("Không thể hủy khi trạng thái thanh toán không là PENDING");
        }
        bookingRequest.setStatus(Enums.BookingStatus.CANCELLED.name());
        contractService.cancelContract(contract);
        bookingRequestRepository.saveAndFlush(bookingRequest);
        softHoldBookingService.releaseSlot(
                bookingRequest.getKol().getId(),
                bookingRequest.getStartAt(),
                bookingRequest.getEndAt()
        );
        BackgroundJob.delete(payment.getId());
        payment.setStatus(Enums.PaymentStatus.CANCELLED.name());
        paymentRepository.saveAndFlush(payment);
        return ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Hủy thanh toán thành công!"))
                .data(null)
                .build();
    }

    @Override
    public ApiResponse<CampaignPaymentDTO> paymentForCampaign(UUID contractPaymentScheduleId) {
        ContractPaymentSchedule contractPaymentSchedule = contractPaymentScheduleRepository.findById(contractPaymentScheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đợt cần thanh toán theo hợp đồng ID: " + contractPaymentScheduleId));
        if (!contractPaymentSchedule.getStatus().equals(Enums.PaymentScheduleStatus.PENDING)) {
            throw new IllegalArgumentException("Thanh toán đã được thực hiện hoặc đã hết hạn!");
        }
        Merchant merchant = merchantService.getMerchantIsActive();
        String transferContent = PAYMENT_TRANSFER_CONTENT_FORMAT + contractPaymentScheduleId;
        String qrUrl = qRGenerateService.createQRCode(contractPaymentSchedule.getAmount(), transferContent);
        CampaignPaymentDTO campaignPaymentDTO = CampaignPaymentDTO.builder()
                .contractId(contractPaymentSchedule.getContract().getId())
                .contractPaymentScheduleId(contractPaymentSchedule.getId())
                .installmentNumber(contractPaymentSchedule.getInstallmentNumber())
                .amount(contractPaymentSchedule.getAmount())
                .qrUrl(qrUrl)
                .transferContent(transferContent)
                .name(merchant.getName())
                .bank(merchant.getBank())
                .accountNumber(merchant.getAccountNumber())
                .build();
        return ApiResponse.<CampaignPaymentDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Khởi tạo mã QR thanh toán thành công!"))
                .data(campaignPaymentDTO)
                .build();
    }

    @Override
    public void updatePaymentForCampaignAfterTransactionSuccess(UUID contractPaymentScheduleId) {
        ContractPaymentSchedule contractPaymentSchedule = contractPaymentScheduleRepository.findById(contractPaymentScheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đợt cần thanh toán theo hợp đồng ID: " + contractPaymentScheduleId));
        contractPaymentSchedule.setStatus(Enums.PaymentScheduleStatus.PAID);
        contractPaymentScheduleRepository.save(contractPaymentSchedule);
    }

    @Override
    public boolean checkContractForCampaignPaymentSuccess(UUID contractPaymentScheduleId) {
        ContractPaymentSchedule contractPaymentSchedule = contractPaymentScheduleRepository.findById(contractPaymentScheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đợt cần thanh toán theo hợp đồng ID: " + contractPaymentScheduleId));
        return contractPaymentSchedule.getStatus().equals(Enums.PaymentScheduleStatus.PAID);
    }
}
