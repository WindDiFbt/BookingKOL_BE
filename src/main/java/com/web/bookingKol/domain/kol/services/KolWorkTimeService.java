package com.web.bookingKol.domain.kol.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.models.KolWorkTimeDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public interface KolWorkTimeService {
    KolWorkTime createNewKolWorkTime(KolAvailability kolAvailability, BookingRequest bookingRequest, String status, Instant startAt, Instant endAt);
    ApiResponse<List<KolWorkTimeDTO>> getWorkTimesByBookingRequest(UUID bookingRequestId);

}
