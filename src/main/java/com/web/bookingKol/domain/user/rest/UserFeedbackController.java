package com.web.bookingKol.domain.user.rest;

import com.web.bookingKol.domain.kol.dtos.feedbacks.NewFeedbackReq;
import com.web.bookingKol.domain.kol.dtos.feedbacks.UpdateFeedbackDTO;
import com.web.bookingKol.domain.kol.models.KolFeedback;
import com.web.bookingKol.domain.kol.repositories.KolFeedbackRepository;
import com.web.bookingKol.domain.kol.services.KolFeedbackService;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user/feedbacks")
public class UserFeedbackController {
    @Autowired
    private KolFeedbackService kolFeedbackService;
    @Autowired
    private KolFeedbackRepository kolFeedbackRepository;

    @GetMapping("/detail/{feedbackId}")
    public ResponseEntity<?> getDetailFeedback(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @PathVariable UUID feedbackId) {
        KolFeedback feedback = kolFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with ID: " + feedbackId));
        if (!feedback.getReviewerUser().getId().equals(userDetails.getId())) {
            throw new IllegalArgumentException("Reviewer Id not match with your ID: " + userDetails.getId());
        }
        return ResponseEntity.ok().body(kolFeedbackService.getDetailFeedback(feedbackId));
    }

    @PostMapping("/create/{contractId}")
    public ResponseEntity<?> createNewFeedback(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @PathVariable UUID contractId,
                                               @RequestBody @Valid NewFeedbackReq newFeedbackReq) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(kolFeedbackService.createNewFeedback(userId, contractId, newFeedbackReq));
    }

    @PatchMapping("/update/{feedbackId}")
    public ResponseEntity<?> updateFeedback(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable UUID feedbackId,
                                            @RequestBody UpdateFeedbackDTO updateFeedbackDTO) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(kolFeedbackService.updateFeedback(feedbackId, userId, updateFeedbackDTO));
    }
}
