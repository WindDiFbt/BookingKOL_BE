package com.web.bookingKol.domain.kol.services.impl;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.models.KolWorkTimeDTO;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import com.web.bookingKol.domain.kol.services.KolWorkTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class KolWorkTimeServiceImpl implements KolWorkTimeService {
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;

    @Override
    public KolWorkTime createNewKolWorkTime(KolAvailability kolAvailability, BookingRequest bookingRequest, String status, Instant startAt, Instant endAt) {
        KolWorkTime kolWorkTime = new KolWorkTime();
        kolWorkTime.setId(UUID.randomUUID());
        kolWorkTime.setAvailability(kolAvailability);
        kolWorkTime.setStartAt(startAt);
        kolWorkTime.setEndAt(endAt);
        kolWorkTime.setStatus(status);
        kolWorkTime.setBookingRequest(bookingRequest);
        kolWorkTimeRepository.save(kolWorkTime);
        return kolWorkTime;
    }

    @Override
    @Transactional
    public ApiResponse<List<KolWorkTimeDTO>> getWorkTimesByBookingRequest(UUID bookingRequestId) {
        List<KolWorkTime> workTimes = kolWorkTimeRepository.findByBookingRequestId(bookingRequestId);

        if (workTimes.isEmpty()) {
            return ApiResponse.<List<KolWorkTimeDTO>>builder()
                    .status(404)
                    .message(List.of("Không tìm thấy ca làm nào cho booking request này"))
                    .build();
        }

        List<KolWorkTimeDTO> data = workTimes.stream()
                .map(KolWorkTimeDTO::new)
                .toList();

        return ApiResponse.<List<KolWorkTimeDTO>>builder()
                .status(200)
                .message(List.of("Lấy danh sách ca làm thành công"))
                .data(data)
                .build();
    }

}
