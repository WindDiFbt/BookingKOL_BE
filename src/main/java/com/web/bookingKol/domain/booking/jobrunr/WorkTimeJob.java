package com.web.bookingKol.domain.booking.jobrunr;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class WorkTimeJob {
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;
    @Autowired
    private BookingRequestRepository bookingRequestRepository;
    private final Logger logger = Logger.getLogger("AUTO_COMPLETE_WORK_TIME_JOB");

    @Transactional
    public void autoCompleteWorkTime(UUID workTimeId) {
        var kolWorkTime = kolWorkTimeRepository.findById(workTimeId)
                .orElse(null);
        if (kolWorkTime == null) {
            logger.warning("Work time not found: " + workTimeId);
            return;
        }
        if (!kolWorkTime.getStatus().equalsIgnoreCase(Enums.KOLWorkTimeStatus.COMPLETED.name())) {
            kolWorkTime.setStatus(Enums.KOLWorkTimeStatus.COMPLETED.name());
            kolWorkTimeRepository.save(kolWorkTime);
            checkAndCompleteBookingRequest(kolWorkTime.getBookingRequest());
            logger.info("[JOBRUNR] Closed Work Time " + kolWorkTime.getId() + " for Booking " + kolWorkTime.getBookingRequest().getId());
        }
    }

    private void checkAndCompleteBookingRequest(BookingRequest bookingRequest) {
        Set<String> finishedStatuses = Set.of(
                Enums.KOLWorkTimeStatus.COMPLETED.name(),
                Enums.KOLWorkTimeStatus.CANCELLED.name()
        );
        boolean allWorkTimesFinished = bookingRequest.getKolWorkTimes().stream()
                .allMatch(kolWorkTime -> finishedStatuses.contains(kolWorkTime.getStatus()));
        if (allWorkTimesFinished) {
            bookingRequest.setStatus(Enums.BookingStatus.COMPLETED.name());
            bookingRequestRepository.saveAndFlush(bookingRequest);
        }
    }
}
