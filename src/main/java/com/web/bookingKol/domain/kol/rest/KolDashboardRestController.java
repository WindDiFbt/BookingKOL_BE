package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.services.KolDashboardService;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/kol/dashboard")
public class KolDashboardRestController {
    @Autowired
    private KolDashboardService kolDashboardService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/summary")
    public ResponseEntity<?> getKolSummary(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {

        UUID userId = userDetails.getId();
        KolProfile kolProfile = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId)).getKolProfile();
        return ResponseEntity.ok(kolDashboardService.getKolSummary(kolProfile.getId(), startDate, endDate));
    }
}
