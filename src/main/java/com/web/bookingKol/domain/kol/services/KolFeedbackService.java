package com.web.bookingKol.domain.kol.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.feedbacks.FeedbackDTO;
import com.web.bookingKol.domain.kol.dtos.feedbacks.NewFeedbackReq;
import com.web.bookingKol.domain.kol.dtos.feedbacks.UpdateFeedbackDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public interface KolFeedbackService {
    ApiResponse<FeedbackDTO> createNewFeedback(UUID reviewerUserId, UUID contractId, NewFeedbackReq newFeedbackReq);

    ApiResponse<Page<FeedbackDTO>> getAllFeedbackOfKol(UUID kolId, Pageable pageable);

    ApiResponse<Page<FeedbackDTO>> getAllFeedbackOfUser(UUID userId, Pageable pageable);

    ApiResponse<FeedbackDTO> getDetailFeedback(UUID feedbackId);

    ApiResponse<Set<FeedbackDTO>> getDetailFeedbackByContract(UUID contractId);

    ApiResponse<FeedbackDTO> updateFeedback(UUID feedbackId, UUID userId, UpdateFeedbackDTO updateFeedbackDTO);

    ApiResponse<FeedbackDTO> hideOrShowFeedback(UUID feedbackId, boolean isPublic);
}
