package com.web.bookingKol.domain.booking.services;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Component
public class AutoCompleteWorkTimeScheduler {
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;
    @Autowired
    private BookingRequestService bookingRequestService;
    private final Logger logger = Logger.getLogger("AUTO_COMPLETE_WORK_TIME_SCHEDULER");

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void autoCloseExpiredWorkTime() {
        Instant cutoffTime = Instant.now().minus(Duration.ofDays(3));
        List<KolWorkTime> expiredKolWorkTimes = kolWorkTimeRepository.findAllKolWorkTimeExpired(cutoffTime);
        if (expiredKolWorkTimes.isEmpty()) {
            logger.info("Not found any expired work time to close.");
            return;
        }
        Set<BookingRequest> affectedBookingRequests = new HashSet<>();
        for (KolWorkTime kolWorkTime : expiredKolWorkTimes) {
            kolWorkTime.setStatus(Enums.KOLWorkTimeStatus.COMPLETED.name());
            affectedBookingRequests.add(kolWorkTime.getBookingRequest());
            logger.info("[SCHEDULER] Closed Work Time " + kolWorkTime.getId() + " for Booking " + kolWorkTime.getBookingRequest().getId());
        }
        kolWorkTimeRepository.saveAll(expiredKolWorkTimes);
        for (BookingRequest bookingRequest : affectedBookingRequests) {
            bookingRequestService.checkAndCompleteBookingRequest(bookingRequest);
        }
    }
}
