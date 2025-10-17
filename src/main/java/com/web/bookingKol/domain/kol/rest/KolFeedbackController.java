package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.domain.kol.services.KolFeedbackService;
import com.web.bookingKol.domain.kol.services.KolProfileService;
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
@RequestMapping("/kol/feedbacks")
public class KolFeedbackController {
    @Autowired
    private KolFeedbackService kolFeedbackService;
    @Autowired
    private KolProfileService kolProfileService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllFeedbacks(@AuthenticationPrincipal UserDetailsImpl userDetails, Pageable pageable) {
        UUID kolId = kolProfileService.getKolProfileEntityByUserId(userDetails.getId()).getId();
        return ResponseEntity.ok().body(kolFeedbackService.getAllFeedbackOfKol(kolId, pageable));
    }

    @GetMapping("/detail/{feedbackId}")
    public ResponseEntity<?> getDetailFeedback(@PathVariable UUID feedbackId) {
        return ResponseEntity.ok().body(kolFeedbackService.getDetailFeedback(feedbackId));
    }
}
