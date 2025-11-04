package com.web.bookingKol.domain.booking.rest;

import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.RejectContractRequest;
import com.web.bookingKol.domain.booking.dtos.SignContractRequest;
import com.web.bookingKol.domain.booking.dtos.UserContractResponse;
import com.web.bookingKol.domain.booking.services.UserContractService;
import com.web.bookingKol.domain.booking.services.impl.ContractPaymentScheduleServiceImpl;
import com.web.bookingKol.domain.booking.services.impl.UserContractServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user/contracts")
@RequiredArgsConstructor
public class UserContractController {

    private final UserContractService userContractService;
    private final ContractPaymentScheduleServiceImpl contractPaymentScheduleServiceImpl;
    private final UserContractServiceImpl userContractServiceImpl;

    @PreAuthorize("hasAnyAuthority('USER')")
    @PutMapping("/sign/{contractId}")
    public ResponseEntity<ApiResponse<?>> signContract(
            @PathVariable UUID contractId,
            @Valid @RequestBody SignContractRequest request
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(
                userContractService.signContract(contractId, request.getBookingRequestId(), email)
        );
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @PutMapping("/reject/{contractId}")
    public ResponseEntity<ApiResponse<?>> rejectContract(
            @PathVariable UUID contractId,
            @Valid @RequestBody RejectContractRequest request
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(
                userContractService.rejectContract(contractId, request.getBookingRequestId(), email, request.getReason())
        );
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PagedResponse<UserContractResponse>>> getUserContracts(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userContractServiceImpl.getUserContracts(email, keyword, pageable));
    }


}

