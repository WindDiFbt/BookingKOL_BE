package com.web.bookingKol.domain.kol.mappers;

import com.web.bookingKol.domain.kol.dtos.feedbacks.FeedbackDTO;
import com.web.bookingKol.domain.kol.models.KolFeedback;
import org.springframework.stereotype.Component;

@Component
public class KolFeedbackMapper {
    public FeedbackDTO toDto(KolFeedback entity) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(entity.getId());
        if (entity.getKol() != null) {
            dto.setKolId(entity.getKol().getId());
        }
        if (entity.getContract() != null) {
            dto.setContractId(entity.getContract().getId());
        }
        if (entity.getReviewerUser() != null) {
            dto.setReviewerUserId(entity.getReviewerUser().getId());
            dto.setReviewerUserName(entity.getReviewerUser().getFullName());
            dto.setReviewerUserAvatarUrl(entity.getReviewerUser().getAvatarUrl());
        }
        dto.setOverallRating(entity.getOverallRating());
        dto.setProfessionalismRating(entity.getProfessionalismRating());
        dto.setCommunicationRating(entity.getCommunicationRating());
        dto.setTimelineRating(entity.getTimelineRating());
        dto.setContentQualityRating(entity.getContentQualityRating());

        dto.setCommentPublic(entity.getCommentPublic());
        dto.setCommentPrivate(entity.getCommentPrivate());
        dto.setWouldRehire(entity.getWouldRehire());

        dto.setIsPublic(entity.getIsPublic());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
