package com.web.bookingKol.domain.payment.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.course.models.PurchasedCoursePackage;
import com.web.bookingKol.domain.payment.dtos.CampaignPaymentDTO;
import com.web.bookingKol.domain.payment.dtos.PaymentReqDTO;
import com.web.bookingKol.domain.payment.dtos.transaction.TransactionDTO;
import com.web.bookingKol.domain.payment.models.Payment;
import com.web.bookingKol.domain.user.models.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public interface PaymentService {
    PaymentReqDTO initiatePayment(BookingRequest bookingRequest, Contract contract, String qrUrl, User user, BigDecimal amount);

    void updatePaymentAfterTransactionSuccess(TransactionDTO transactionDTO);

    void updateCoursePaymentAfterTransactionSuccess(TransactionDTO transactionDTO);

    boolean checkContractPaymentSuccess(UUID contractId);

    boolean checkPurchasedCoursePackagePaymentSuccess(UUID purchasedCoursePackageId);

    boolean checkContractForCampaignPaymentSuccess(UUID contractPaymentScheduleId);

    Payment initiateCoursePayment(PurchasedCoursePackage purchasedCoursePackage, User user, Long currentPrice);

    PaymentReqDTO createCoursePaymentRequest(PurchasedCoursePackage purchasedCoursePackage, Payment payment, String qrUrl);

    ApiResponse<?> cancelPaymentBookingRequest(UUID userId, UUID bookingRequestID);

    ApiResponse<CampaignPaymentDTO> paymentForCampaign(UUID contractPaymentScheduleId);

    void updatePaymentForCampaignAfterTransactionSuccess(UUID contractPaymentScheduleId);

}
