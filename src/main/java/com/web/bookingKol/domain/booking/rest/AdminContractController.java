package com.web.bookingKol.domain.booking.rest;

import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.UserContractResponse;
import com.web.bookingKol.domain.booking.services.AdminContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/contracts")
@RequiredArgsConstructor
public class AdminContractController {

    private final AdminContractService adminContractService;

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserContractResponse>>> getAllContracts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(adminContractService.getAllContracts(keyword, pageable));
    }
}

