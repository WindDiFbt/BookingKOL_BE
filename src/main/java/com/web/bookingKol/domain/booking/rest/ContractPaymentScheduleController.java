package com.web.bookingKol.domain.booking.rest;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.CreatePaymentScheduleRequest;
import com.web.bookingKol.domain.booking.services.ContractPaymentScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/contracts/payments")
@RequiredArgsConstructor
public class ContractPaymentScheduleController {

    private final ContractPaymentScheduleService scheduleService;

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createSchedule(
            @Valid @RequestBody CreatePaymentScheduleRequest request
    ) {
        return ResponseEntity.ok(scheduleService.createPaymentSchedule(request));
    }
}

