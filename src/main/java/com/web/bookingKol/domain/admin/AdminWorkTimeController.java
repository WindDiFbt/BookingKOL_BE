package com.web.bookingKol.domain.admin;

import com.web.bookingKol.domain.booking.services.LivestreamMetricService;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/requests/worktime")
public class AdminWorkTimeController {
    @Autowired
    private LivestreamMetricService livestreamMetricService;

    @GetMapping("/livestream-metrics/{worktimeId}")
    public ResponseEntity<?> getLivestreamMetricDetail(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                       @PathVariable UUID worktimeId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok(livestreamMetricService.getDetailLivestreamMetricByKolWorkTimeId(userId, worktimeId));
    }

    @GetMapping("/livestream-metrics/kol/{kolId}")
    public ResponseEntity<?> getLivestreamMetricOfKol(@PathVariable UUID kolId, Pageable pageable) {
        return ResponseEntity.ok(livestreamMetricService.getLivestreamMetricOfKol(kolId, pageable));
    }
}
