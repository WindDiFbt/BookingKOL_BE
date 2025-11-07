package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.KolCancelRequestDTO;
import com.web.bookingKol.domain.kol.services.KolCancelRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;
import java.util.UUID;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class KolCancelRequestController {

    private final KolCancelRequestService cancelService;

    // KOL gửi yêu cầu hủy ca
    @PreAuthorize("hasAuthority('KOL')")
    @PostMapping("/kol/request")
    public ResponseEntity<ApiResponse<KolCancelRequestDTO>> requestCancelWorkTime(
            Authentication authentication,
            @RequestBody KolCancelRequestDTO dto
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                cancelService.requestCancelWorkTimeByEmail(email, dto.getWorkTimeId(), dto.getReason())
        );
    }



    // Admin duyệt yêu cầu hủy
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PostMapping("/admin/approve/{requestId}")
    public ResponseEntity<ApiResponse<String>> approveCancelRequest(
            @PathVariable UUID requestId,
            @RequestParam(required = false) String adminNote
    ) {
        return ResponseEntity.ok(cancelService.approveCancelRequest(requestId, adminNote));
    }

    // Admin từ chối
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PostMapping("/admin/reject/{requestId}")
    public ResponseEntity<ApiResponse<String>> rejectCancelRequest(
            @PathVariable UUID requestId,
            @RequestParam(required = false) String adminNote
    ) {
        return ResponseEntity.ok(cancelService.rejectCancelRequest(requestId, adminNote));
    }
}

