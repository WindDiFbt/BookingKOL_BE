package com.web.bookingKol.domain.payment.services;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.payment.models.Payment;
import com.web.bookingKol.domain.payment.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Component
public class PaymentMonitorScheduler {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRequestRepository bookingRequestRepository;
    private final Logger logger = Logger.getLogger("PAYMENT_MONITOR_SCHEDULER");

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredPayments() {
        Instant now = Instant.now();
        List<Payment> expiredPayments = paymentRepository.findPendingAndExpired(now);
        if (expiredPayments.isEmpty()) {
            logger.info("Not found any expired pending payments to close.");
            return;
        }
        logger.info("Found " + expiredPayments.size() + " expired pending payments to close.");
        for (Payment payment : expiredPayments) {
            BookingRequest bookingRequest = payment.getContract().getBookingRequest();
            if (bookingRequest.getStatus().equals(Enums.BookingStatus.REQUESTED.name())) {
                payment.setStatus(Enums.PaymentStatus.EXPIRED.name());
                payment.setFailureReason("Payment expired after 15 minutes timeout.");
                payment.setUpdatedAt(now);
                paymentRepository.save(payment);
                payment.getContract().setStatus(Enums.ContractStatus.EXPIRED.name());
                bookingRequest.setStatus(Enums.BookingStatus.EXPIRED.name());
                bookingRequestRepository.save(bookingRequest);
                Set<KolWorkTime> kolWorkTime = bookingRequest.getKolWorkTimes();
                for (KolWorkTime workTime : kolWorkTime) {
                    if (workTime.getStatus().equals(Enums.BookingStatus.REQUESTED.name())) {
                        workTime.setStatus(Enums.KOLWorkTimeStatus.CANCELLED.name());
                        bookingRequestRepository.save(bookingRequest);
                    }
                }
                logger.info("[SCHEDULER] Closed Payment " + payment.getId() + " and freed slot for Booking " + bookingRequest.getId());
            }
        }
    }
}
