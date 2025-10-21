package com.web.bookingKol.domain.booking.services;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.booking.dtos.BookingSingleReqDTO;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.repositories.KolAvailabilityRepository;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class BookingValidationService {
    @Autowired
    private KolAvailabilityRepository kolAvailabilityRepository;
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;

    public void validateBookingRequest(BookingSingleReqDTO bookingRequestDTO, KolProfile kol) {
        if (bookingRequestDTO.getIsConfirmWithTerms() == false) {
            throw new IllegalArgumentException("You must agree to the terms before continuing.");
        }
        Instant now = Instant.now();
        Instant startAt = bookingRequestDTO.getStartAt();
        Instant endAt = bookingRequestDTO.getEndAt();
        //Validate that the time range is valid (correct order, full hours, within limits)
        validateTime(startAt, endAt);
        //Enforce minimum booking lead time (at least 3 days in advance)
        if (startAt.isBefore(now.plus(3, ChronoUnit.DAYS))) {
            throw new IllegalArgumentException("Bookings must be made at least 3 days in advance.");
        }
        //Check if the KOL is available during the requested time range
        if (!kolAvailabilityRepository.isKolAvailabilityInRange(kol.getId(), startAt, endAt)) {
            throw new IllegalArgumentException("KOL is not available for that time!");
        }
        //Check for any existing booking requests with the exact same start & end times
        if (kolWorkTimeRepository.existsRequestSameTime(kol.getId(), startAt, endAt)) {
            throw new IllegalArgumentException("Already exist a booking request for this KOL at this time!");
        }
        //Check for overlapping bookings
        // Correct logic: overlap exists if (start < existing.end) AND (end > existing.start)
        // If this condition is true → conflict detected → reject the new booking
        if (kolWorkTimeRepository.existsOverlappingBooking(kol.getId(), startAt, endAt)) {
            throw new IllegalArgumentException("Overlapping Bookings for this KOL at this time!");
        }
        //Enforce a minimum 1-hour break between consecutive bookings
        KolWorkTime kolWorkTimePrevious = kolWorkTimeRepository.findFirstPreviousBooking(kol.getId(), startAt)
                .stream().findFirst().orElse(null);
        KolWorkTime kolWorkTimeNext = kolWorkTimeRepository.findNextBooking(kol.getId(), endAt)
                .stream().findFirst().orElse(null);
        //Check the booking before/after the current one
        if (kolWorkTimePrevious != null && startAt.isBefore(kolWorkTimePrevious.getEndAt().plus(Enums.BookingRules.REST_TIME.getValue(), ChronoUnit.HOURS))) {
            throw new IllegalArgumentException("KOLs need at least 1 hour break between shifts.");
        }
        if (kolWorkTimeNext != null && endAt.isAfter(kolWorkTimeNext.getStartAt().minus(Enums.BookingRules.REST_TIME.getValue(), ChronoUnit.HOURS))) {
            throw new IllegalArgumentException("KOLs need at least 1 hour break between shifts.");
        }
    }

    private void validateTime(Instant start, Instant end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Invalid start/end time.");
        }
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes % 60 != 0) {
            throw new IllegalArgumentException("Booking must be in full hours.");
        }
        long hours = minutes / 60;
        final long MIN_HOURS = Enums.BookingRules.MIN_BOOKING_TIME.getValue();
        final long MAX_HOURS = Enums.BookingRules.MAX_BOOKING_TIME.getValue();
        if (hours < MIN_HOURS || hours > MAX_HOURS) {
            throw new IllegalArgumentException("Booking length must be between " + MIN_HOURS + " and " + MAX_HOURS + " hours.");
        }
    }
}
