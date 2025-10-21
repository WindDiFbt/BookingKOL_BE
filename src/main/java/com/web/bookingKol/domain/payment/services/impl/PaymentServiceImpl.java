package com.web.bookingKol.domain.payment.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.payment.dtos.PaymentReqDTO;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionDTO;
import com.web.bookingKol.domain.payment.models.Merchant;
import com.web.bookingKol.domain.payment.models.Payment;
import com.web.bookingKol.domain.payment.repositories.PaymentRepository;
import com.web.bookingKol.domain.payment.services.MerchantService;
import com.web.bookingKol.domain.payment.services.PaymentService;
import com.web.bookingKol.domain.user.models.User;
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
    private MerchantService merchantService;

    private final String VND_CURRENCY = "VND";
    private final Integer EXPIRES_TIME = 15;

    @Override
    public PaymentReqDTO initiatePayment(BookingRequest bookingRequest, Contract contract, String qrUrl, User user, BigDecimal amount) {
        Merchant merchant = merchantService.getMerchantIsActive();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setContract(contract);
        payment.setUser(user);
        payment.setTotalAmount(amount);
        payment.setCurrency(VND_CURRENCY);
        payment.setStatus(Enums.PaymentStatus.PENDING.name());
        payment.setPaidAmount(null);
        payment.setFailureReason(null);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(null);
        payment.setExpiresAt(Instant.now().plus(EXPIRES_TIME, ChronoUnit.MINUTES));
        paymentRepository.save(payment);
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
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

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
            bookingRequest.setStatus(Enums.BookingStatus.ACCEPTED.name());
            bookingRequestRepository.save(bookingRequest);
            Set<KolWorkTime> kolWorkTime = bookingRequest.getKolWorkTimes();
            for (KolWorkTime workTime : kolWorkTime) {
                if (workTime.getStatus().equals(Enums.BookingStatus.REQUESTED.name())) {
                    workTime.setStatus(Enums.KOLWorkTimeStatus.COMPLETED.name());
                    bookingRequestRepository.save(bookingRequest);
                }
            }
        }
        payment.setStatus(status);
        paymentRepository.save(payment);
    }

    @Override
    public boolean checkContractPaymentSuccess(UUID contractId) {
        Payment payment = paymentRepository.findByContractId(contractId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found");
        }
        return payment.getStatus().equals(Enums.PaymentStatus.PAID.name()) || payment.getStatus().equals(Enums.PaymentStatus.OVERPAID.name());
    }
}
