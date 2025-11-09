package com.web.bookingKol.domain.kol.rest;


import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.*;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.models.KolWorkTimeDTO;
import com.web.bookingKol.domain.kol.services.KolAvailabilityService;
import com.web.bookingKol.domain.kol.services.impl.KolAvailabilityServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/availabilities")
public class KolAvailabilityController {

    @Autowired
    private KolAvailabilityService availabilityService;

    @Autowired
    private KolAvailabilityServiceImpl kolAvailabilityServiceImpl;

    @Autowired
    private KolAvailabilityService kolAvailabilityService;

//    @PreAuthorize("hasAuthority('KOL')")
//    @GetMapping("/{kolId}/schedule")
//    public ResponseEntity<ApiResponse<List<KolAvailabilityDTO>>> getKolSchedule(
//            @PathVariable UUID kolId,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end
//    ) {
//        return ResponseEntity.ok(availabilityService.getKolSchedule(kolId, start, end));
//    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN','KOL','LIVE')")
    @PostMapping("/schedule/{kolId}")
    public ResponseEntity<ApiResponse<KolAvailabilityDTO>> createKolAvailability(
            @PathVariable UUID kolId,
            @RequestBody KolAvailabilityDTO availabilityDTO
    ) {

        return ResponseEntity.ok(availabilityService.createKolSchedule(kolId,availabilityDTO));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN','KOL','LIVE')")
    @GetMapping("/time-line/{availabilityId}")
    public ResponseEntity<ApiResponse<KolAvailabilityDTO>> getKolAvailabilityById(
            @PathVariable UUID availabilityId
    ) {
        return ResponseEntity.ok(availabilityService.getKolAvailabilityById(availabilityId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN','KOL','LIVE')")
    @GetMapping("/time-line/kol/{kolId}")
    public ResponseEntity<ApiResponse<List<KolAvailabilityDTO>>> getKolAvailabilitiesByKol(
            @PathVariable UUID kolId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(availabilityService.getKolAvailabilitiesByKol(kolId, startDate, endDate, page, size));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN','USER','LIVE','KOL')")
    @GetMapping("/free-time/{kolId}")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getKolFreeTimes(
            @PathVariable UUID kolId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate
    ) {
        return ResponseEntity.ok(
                kolAvailabilityServiceImpl.getKolFreeTimes(kolId, startDate, endDate, null)
        );
    }


    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PutMapping("/schedule/{workTimeId}")
    public ResponseEntity<ApiResponse<KolWorkTimeDTO>> updateKolWorkTime(
            @PathVariable UUID workTimeId,
            @RequestBody KolWorkTimeDTO dto
    ) {
        return ResponseEntity.ok(kolAvailabilityService.updateKolWorkTimeByAdmin(workTimeId, dto));
    }

    // api admin thêm lịch làm việc cho kol
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PostMapping("/admin/schedule")
    public ResponseEntity<ApiResponse<KolAvailabilityDTO>> createKolScheduleByAdmin(
            @RequestBody KolAvailabilityDTO dto
    ) {
        return ResponseEntity.ok(availabilityService.createKolScheduleByAdmin(dto));
    }


    // admin xóa lịch rảnh của kol
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/admin/schedule/{availabilityId}")
    public ResponseEntity<ApiResponse<String>> deleteKolAvailabilityByAdmin(
            @PathVariable UUID availabilityId
    ) {
        return ResponseEntity.ok(availabilityService.deleteKolAvailabilityByAdmin(availabilityId));
    }

    @PreAuthorize("hasAuthority('KOL')")
    @PutMapping("/kol/remove-range")
    public ResponseEntity<ApiResponse<String>> removeAvailabilityRange(
            Authentication authentication,
            @RequestBody TimeRangeDTO request
    ) {
        String email = authentication.getName();

        TimeRangeDTO timeRange = new TimeRangeDTO();
        timeRange.setStartRemove(request.getStartRemove());
        timeRange.setEndRemove(request.getEndRemove());

        return ResponseEntity.ok(
                availabilityService.removeAvailabilityRange(email, request.getAvailabilityId(), timeRange)
        );
    }


    // admin thêm lịch rảnh
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/admin/add")
    public ResponseEntity<ApiResponse<String>> adminAddAvailability(
            @RequestBody AdminCreateAvailabilityDTO dto
    ) {
        return ResponseEntity.ok(availabilityService.adminCreateAvailability(dto));
    }


    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PostMapping("/admin/worktime/create")
    public ResponseEntity<ApiResponse<?>> createKolWorkTimeByAdmin(@RequestBody KolWorkTimeCreateDTO dto) {
        return ResponseEntity.ok(kolAvailabilityService.createKolWorkTimeByAdmin(dto));
    }

    @PreAuthorize("hasAuthority('KOL')")
    @PutMapping("/kol/update")
    public ResponseEntity<ApiResponse<KolAvailabilityDTO>> updateKolAvailability(
            Authentication authentication,
            @RequestBody KolAvailabilityUpdateDTO dto
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(availabilityService.updateKolAvailability(email, dto));
    }









}