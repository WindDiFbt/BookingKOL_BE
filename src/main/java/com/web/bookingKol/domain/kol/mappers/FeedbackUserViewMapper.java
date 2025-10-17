package com.web.bookingKol.domain.kol.mappers;

import com.web.bookingKol.domain.kol.dtos.feedbacks.FeedbackUserViewDTO;
import com.web.bookingKol.domain.kol.models.KolFeedback;
import org.springframework.stereotype.Component;

@Component
public class FeedbackUserViewMapper {

    public FeedbackUserViewDTO toDto(KolFeedback entity) {
        FeedbackUserViewDTO dto = new FeedbackUserViewDTO();
        dto.setId(entity.getId());
        if (entity.getKol() != null) {
            dto.setKolId(entity.getKol().getId());
        }
        if (entity.getReviewerUser() != null) {
            dto.setReviewerUserName(entity.getReviewerUser().getFullName());
            dto.setReviewerUserAvatarUrl(entity.getReviewerUser().getAvatarUrl());
        }
        dto.setOverallRating(entity.getOverallRating());
        dto.setProfessionalismRating(entity.getProfessionalismRating());
        dto.setCommunicationRating(entity.getCommunicationRating());
        dto.setTimelineRating(entity.getTimelineRating());
        dto.setContentQualityRating(entity.getContentQualityRating());

        dto.setCommentPublic(entity.getCommentPublic());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
