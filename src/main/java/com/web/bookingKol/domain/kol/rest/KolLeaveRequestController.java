package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.KolLeaveRequestDTO;
import com.web.bookingKol.domain.kol.services.KolLeaveRequestService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;
import java.util.UUID;

@RestController
@RequestMapping("/leave-requests")
@RequiredArgsConstructor
public class KolLeaveRequestController {

    private final KolLeaveRequestService leaveService;
    private final UserRepository userRepository;

    @PreAuthorize("hasAuthority('KOL')")
    @PostMapping("/{kolId}/{availabilityId}")
    public ResponseEntity<ApiResponse<KolLeaveRequestDTO>> requestLeave(
            @PathVariable UUID kolId,
            @PathVariable UUID availabilityId,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(leaveService.requestLeave(kolId, availabilityId, reason));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PostMapping("/approve/{leaveRequestId}")
    public ResponseEntity<ApiResponse<String>> approveLeave(
            @PathVariable UUID leaveRequestId,
            @RequestParam(required = false) String adminNote
    ) {
        return ResponseEntity.ok(leaveService.approveLeave(leaveRequestId, adminNote));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PostMapping("/reject/{leaveRequestId}")
    public ResponseEntity<ApiResponse<String>> rejectLeave(
            @PathVariable UUID leaveRequestId,
            @RequestParam(required = false) String adminNote
    ) {
        return ResponseEntity.ok(leaveService.rejectLeave(leaveRequestId, adminNote));
    }


    @PreAuthorize("hasAuthority('KOL')")
    @GetMapping("/kol/my-leaves")
    public ResponseEntity<ApiResponse<Page<KolLeaveRequestDTO>>> getMyLeaveRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        String email = authentication.getName(); // sub trong JWT là email
        UUID userId = userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng: " + email));

        return ResponseEntity.ok(leaveService.getMyLeaveRequests(userId, page, size, keyword));
    }





    // admin xem ds đơn nghỉ
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @GetMapping("/admin/leaves")
    public ResponseEntity<ApiResponse<Page<KolLeaveRequestDTO>>> getAllLeaveRequestsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(leaveService.getAllLeaveRequestsForAdmin(page, size, keyword, status));
    }


}

