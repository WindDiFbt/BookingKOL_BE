package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.KolAvailabilityDTO;
import com.web.bookingKol.domain.kol.services.KolAvailabilityService;
import com.web.bookingKol.domain.kol.services.impl.KolAvailabilityServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/kol/availabilities")
@RequiredArgsConstructor
public class KolAvailabilityController {

    private final KolAvailabilityService availabilityService;

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','KOL')")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<KolAvailabilityDTO>>> getKolSchedule(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(availabilityService.getKolSchedule(userId, start, end, status, note));
    }

    @PreAuthorize("hasAnyRole('KOL')")
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<KolAvailabilityDTO>> createAvailability(
            @PathVariable UUID userId,
            @RequestBody KolAvailabilityDTO dto
    ) {
        return ResponseEntity.ok(availabilityService.createAvailability(userId, dto));
    }

    @PreAuthorize("hasAnyRole('KOL')")
    @PatchMapping("/{userId}/{availabilityId}")
    public ResponseEntity<ApiResponse<KolAvailabilityDTO>> updateAvailability(
            @PathVariable UUID userId,
            @PathVariable UUID availabilityId,
            @RequestBody KolAvailabilityDTO dto
    ) {
        return ResponseEntity.ok(availabilityService.updateAvailability(userId, availabilityId, dto));
    }

    @PreAuthorize("hasAnyRole('KOL')")
    @DeleteMapping("/{userId}/{availabilityId}")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(
            @PathVariable UUID userId,
            @PathVariable UUID availabilityId
    ) {
        return ResponseEntity.ok(availabilityService.deleteAvailability(userId, availabilityId));
    }
}
