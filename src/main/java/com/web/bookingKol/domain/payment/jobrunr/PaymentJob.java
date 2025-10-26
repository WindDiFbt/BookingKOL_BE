package com.web.bookingKol.domain.payment.jobrunr;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.payment.models.Payment;
import com.web.bookingKol.domain.payment.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class PaymentJob {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BookingRequestRepository bookingRequestRepository;
    private final Logger logger = Logger.getLogger("PAYMENT_EXPIRATION_JOB");

    @Transactional
    public void expirePayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            logger.warning("Payment not found: " + paymentId);
            return;
        }
        if (!payment.getStatus().equals(Enums.PaymentStatus.PENDING.name())) {
            logger.info("Payment " + paymentId + " already processed or expired.");
            return;
        }
        BookingRequest bookingRequest = payment.getContract().getBookingRequest();
        Instant now = Instant.now();
        //Payment
        payment.setStatus(Enums.PaymentStatus.EXPIRED.name());
        payment.setFailureReason("Payment expired after 15 minutes timeout.");
        payment.setUpdatedAt(now);
        paymentRepository.save(payment);
        //BookingRequest
        bookingRequest.setStatus(Enums.BookingStatus.EXPIRED.name());
        bookingRequestRepository.save(bookingRequest);
        //Contract
        payment.getContract().setStatus(Enums.ContractStatus.EXPIRED.name());
        //KolWorkTime
        Set<KolWorkTime> kolWorkTime = bookingRequest.getKolWorkTimes();
        for (KolWorkTime workTime : kolWorkTime) {
            if (workTime.getStatus().equals(Enums.BookingStatus.REQUESTED.name())) {
                workTime.setStatus(Enums.KOLWorkTimeStatus.CANCELLED.name());
            }
        }
        logger.info("[JOBRUNR] Expired payment " + paymentId + " and freed slot for Booking " + bookingRequest.getId());
    }
}
