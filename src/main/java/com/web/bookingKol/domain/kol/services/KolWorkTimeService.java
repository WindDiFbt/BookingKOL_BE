package com.web.bookingKol.domain.kol.services;

import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public interface KolWorkTimeService {
    void createNewKolWorkTime(KolAvailability kolAvailability, BookingRequest bookingRequest, String status, Instant startAt, Instant endAt);
}
