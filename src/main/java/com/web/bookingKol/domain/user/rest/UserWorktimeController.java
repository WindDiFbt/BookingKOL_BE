package com.web.bookingKol.domain.user.rest;

import com.web.bookingKol.domain.booking.services.LivestreamMetricService;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user/requests/worktime")
public class UserWorktimeController {
    @Autowired
    private LivestreamMetricService livestreamMetricService;

    @GetMapping("/livestream-metrics/{worktimeId}")
    public ResponseEntity<?> getLivestreamMetricsOfWorkTime(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                            @PathVariable UUID worktimeId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(livestreamMetricService.getDetailLivestreamMetricByKolWorkTimeId(userId, worktimeId));
    }

    @PatchMapping("/livestream-metrics/confirm/{worktimeId}")
    public ResponseEntity<?> confirmBookingRequest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @PathVariable UUID worktimeId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(livestreamMetricService.confirmLivestreamMetric(userId, worktimeId));
    }
}
