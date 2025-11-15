package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.livestreamMetric.LivestreamMetricReqDTO;
import com.web.bookingKol.domain.booking.services.LivestreamMetricService;
import com.web.bookingKol.domain.kol.models.KolWorkTimeDTO;
import com.web.bookingKol.domain.kol.services.KolWorkTimeService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/kol/requests/worktime")
public class KolWorkTimeController {
    @Autowired
    private LivestreamMetricService livestreamMetricService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create-livestream-metric/{workTimeId}")
    public ResponseEntity<?> createLivestreamMetric(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @PathVariable UUID workTimeId,
                                                    @RequestBody @Valid LivestreamMetricReqDTO livestreamMetricReqDTO) {
        UUID userId = userDetails.getId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(livestreamMetricService.createLivestreamMetric(user.getKolProfile().getId(), workTimeId, livestreamMetricReqDTO));
    }

    @GetMapping("/livestream-metrics/{workTimeId}")
    public ResponseEntity<?> getLivestreamMetricDetail(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                       @PathVariable UUID workTimeId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok(livestreamMetricService.KolGetDetailLivestreamMetricByKolWorkTimeId(userId, workTimeId));
    }
}
