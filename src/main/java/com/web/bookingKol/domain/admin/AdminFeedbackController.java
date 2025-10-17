package com.web.bookingKol.domain.admin;

import com.web.bookingKol.domain.kol.services.KolFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/feedbacks")
public class AdminFeedbackController {
    @Autowired
    private KolFeedbackService kolFeedbackService;

    @GetMapping("/kol/{kolId}")
    public ResponseEntity<?> getAllFeedbackOfKol(@PathVariable UUID kolId, Pageable pageable) {
        return ResponseEntity.ok().body(kolFeedbackService.getAllFeedbackOfKol(kolId, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllFeedbackOfUser(@PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok().body(kolFeedbackService.getAllFeedbackOfUser(userId, pageable));
    }

    @GetMapping("/detail/{feedbackId}")
    public ResponseEntity<?> getDetailFeedback(@PathVariable UUID feedbackId) {
        return ResponseEntity.ok().body(kolFeedbackService.getDetailFeedback(feedbackId));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<?> getDetailFeedbackByContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok().body(kolFeedbackService.getDetailFeedbackByContract(contractId));
    }

    @PatchMapping("/hide/{feedbackId}")
    public ResponseEntity<?> hideFeedback(@PathVariable UUID feedbackId) {
        return ResponseEntity.ok().body(kolFeedbackService.hideOrShowFeedback(feedbackId, false));
    }

    @PatchMapping("/show/{feedbackId}")
    public ResponseEntity<?> showFeedback(@PathVariable UUID feedbackId) {
        return ResponseEntity.ok().body(kolFeedbackService.hideOrShowFeedback(feedbackId, true));
    }
}
