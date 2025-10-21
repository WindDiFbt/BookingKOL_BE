package com.web.bookingKol.domain.kol.services.impl;

import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import com.web.bookingKol.domain.kol.services.KolWorkTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class KolWorkTimeServiceImpl implements KolWorkTimeService {
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;

    @Override
    public void createNewKolWorkTime(KolAvailability kolAvailability, BookingRequest bookingRequest, String status, Instant startAt, Instant endAt) {
        KolWorkTime kolWorkTime = new KolWorkTime();
        kolWorkTime.setId(UUID.randomUUID());
        kolWorkTime.setAvailability(kolAvailability);
        kolWorkTime.setStartAt(startAt);
        kolWorkTime.setEndAt(endAt);
        kolWorkTime.setStatus(status);
        kolWorkTime.setBookingRequest(bookingRequest);
        kolWorkTimeRepository.save(kolWorkTime);
    }
}
