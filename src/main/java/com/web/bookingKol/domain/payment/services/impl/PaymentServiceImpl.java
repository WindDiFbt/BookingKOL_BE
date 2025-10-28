package com.web.bookingKol.domain.payment.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.jobrunr.ReminderEmailJob;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.course.models.PurchasedCoursePackage;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import com.web.bookingKol.domain.payment.dtos.PaymentReqDTO;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionDTO;
import com.web.bookingKol.domain.payment.jobrunr.PaymentJob;
import com.web.bookingKol.domain.payment.models.Merchant;
import com.web.bookingKol.domain.payment.models.Payment;
import com.web.bookingKol.domain.payment.repositories.PaymentRepository;
import com.web.bookingKol.domain.payment.services.MerchantService;
import com.web.bookingKol.domain.payment.services.PaymentService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.PurchasedCoursePackageRepository;
import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    private final String CURRENCY = "VND";
    private final Integer EXPIRES_TIME = 2;

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
            BookingRequest bookingRequest = payment.getContract().getBookingRequest();
            bookingRequest.setStatus(Enums.BookingStatus.IN_PROGRESS.name());
            bookingRequestRepository.save(bookingRequest);
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
            purchasedCoursePackage.setStatus(Enums.PurchasedCourse.COURSEASSIGNED.name());
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
}
