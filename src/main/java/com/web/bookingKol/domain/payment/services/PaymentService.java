package com.web.bookingKol.domain.payment.services;

import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.payment.dtos.PaymentReqDTO;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionDTO;
import com.web.bookingKol.domain.user.models.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface PaymentService {
    PaymentReqDTO initiatePayment(BookingRequest bookingRequest, Contract contract, String qrUrl, User user, BigDecimal amount);

    void updatePaymentAfterTransactionSuccess(TransactionDTO transactionDTO);
}
