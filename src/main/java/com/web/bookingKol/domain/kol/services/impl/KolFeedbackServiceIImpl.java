package com.web.bookingKol.domain.kol.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.UpdateEntityUtil;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.kol.dtos.feedbacks.FeedbackDTO;
import com.web.bookingKol.domain.kol.dtos.feedbacks.NewFeedbackReq;
import com.web.bookingKol.domain.kol.dtos.feedbacks.UpdateFeedbackDTO;
import com.web.bookingKol.domain.kol.mappers.KolFeedbackMapper;
import com.web.bookingKol.domain.kol.models.KolFeedback;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.repositories.KolFeedbackRepository;
import com.web.bookingKol.domain.kol.repositories.KolProfileRepository;
import com.web.bookingKol.domain.kol.services.KolFeedbackService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KolFeedbackServiceIImpl implements KolFeedbackService {
    @Autowired
    private KolFeedbackRepository kolFeedbackRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private KolProfileRepository kolProfileRepository;
    @Autowired
    private KolFeedbackMapper kolFeedbackMapper;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ApiResponse<FeedbackDTO> createNewFeedback(UUID reviewerUserId, UUID contractId, NewFeedbackReq newFeedbackReq) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with ID: " + contractId));
        KolProfile kol = kolProfileRepository.findById(contract.getBookingRequest().getKol().getId())
                .orElseThrow(() -> new EntityNotFoundException("Kol not found with ID: " + contract.getBookingRequest().getKol().getId()));
        User user = userRepository.findById(reviewerUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + reviewerUserId));
        validateRelationship(user, contract);

        KolFeedback kolFeedback = new KolFeedback();
        kolFeedback.setId(UUID.randomUUID());
        kolFeedback.setContract(contract);
        kolFeedback.setKol(kol);
        kolFeedback.setReviewerUser(user);
        kolFeedback.setProfessionalismRating(newFeedbackReq.getProfessionalismRating());
        kolFeedback.setCommunicationRating(newFeedbackReq.getCommunicationRating());
        kolFeedback.setTimelineRating(newFeedbackReq.getTimelineRating());
        kolFeedback.setContentQualityRating(newFeedbackReq.getContentQualityRating());
        double overallRating = (
                newFeedbackReq.getProfessionalismRating() +
                        newFeedbackReq.getCommunicationRating() +
                        newFeedbackReq.getTimelineRating() +
                        newFeedbackReq.getContentQualityRating()) / 4.0;
        BigDecimal bd = BigDecimal.valueOf(overallRating).setScale(2, RoundingMode.HALF_UP);
        Double finalRating = bd.doubleValue();
        kolFeedback.setOverallRating(finalRating);
        kolFeedback.setWouldRehire(newFeedbackReq.getWouldRehire());
        kolFeedback.setCommentPublic(newFeedbackReq.getCommentPublic());
        kolFeedback.setCommentPrivate(newFeedbackReq.getCommentPrivate());
        kolFeedback.setIsPublic(newFeedbackReq.getIsPublic());
        kolFeedback.setCreatedAt(Instant.now());
        kolFeedbackRepository.save(kolFeedback);
        FeedbackDTO feedbackDTO = kolFeedbackMapper.toDto(kolFeedback);
        feedbackDTO.setReviewerUserName(user.getFullName());
        feedbackDTO.setReviewerUserAvatarUrl(user.getAvatarUrl());
        return ApiResponse.<FeedbackDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Feedback created successfully!"))
                .data(feedbackDTO)
                .build();
    }

    @Override
    public ApiResponse<Page<FeedbackDTO>> getAllFeedbackOfKol(UUID kolId, Pageable pageable) {
        if (!kolProfileRepository.existsById(kolId)) {
            throw new EntityNotFoundException("Kol not found with ID: " + kolId);
        }
        Page<KolFeedback> kolFeedbacks = kolFeedbackRepository.getAllByKolId(kolId, pageable);
        Page<FeedbackDTO> feedbackDTOSPage = kolFeedbacks.map(kolFeedbackMapper::toDto);
        return ApiResponse.<Page<FeedbackDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get all feedbacks of KOL successfully!"))
                .data(feedbackDTOSPage)
                .build();
    }

    @Override
    public ApiResponse<Page<FeedbackDTO>> getAllFeedbackOfUser(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
        Page<KolFeedback> kolFeedbacksPage = kolFeedbackRepository.getAllByUserId(userId, pageable);
        Page<FeedbackDTO> feedbackDTOSPage = kolFeedbacksPage.map(kolFeedbackMapper::toDto);
        return ApiResponse.<Page<FeedbackDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get all feedbacks of User successfully!"))
                .data(feedbackDTOSPage)
                .build();
    }

    @Override
    public ApiResponse<FeedbackDTO> getDetailFeedback(UUID feedbackId) {
        KolFeedback feedback = kolFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("Feedback not found with ID: " + feedbackId));
        FeedbackDTO feedbackDTO = kolFeedbackMapper.toDto(feedback);
        return ApiResponse.<FeedbackDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get detail feedback successfully!"))
                .data(feedbackDTO)
                .build();
    }

    @Override
    public ApiResponse<Set<FeedbackDTO>> getDetailFeedbackByContract(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with ID: " + contractId));
        Set<FeedbackDTO> feedbacks = contract.getKolFeedbacks().stream().map(kolFeedbackMapper::toDto).collect(Collectors.toSet());
        if (feedbacks.isEmpty()) {
            throw new EntityNotFoundException("Feedback not found or not exist with contract ID: " + contractId);
        }
        return ApiResponse.<Set<FeedbackDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get feedback of KOL successfully!"))
                .data(feedbacks)
                .build();
    }

    @Override
    public ApiResponse<FeedbackDTO> updateFeedback(UUID feedbackId, UUID reviewerUserId, UpdateFeedbackDTO updateFeedbackDTO) {
        KolFeedback feedback = kolFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("Feedback not found with ID: " + feedbackId));
        if (!feedback.getReviewerUser().getId().equals(reviewerUserId)) {
            throw new EntityNotFoundException("Reviewer Id not match with ID: " + reviewerUserId);
        }
        if (feedback.getCreatedAt().plus(7, ChronoUnit.DAYS).isBefore(Instant.now())) {
            throw new IllegalStateException("Feedback modification window has expired (7 days).");
        }
        BeanUtils.copyProperties(updateFeedbackDTO, feedback, UpdateEntityUtil.getNullPropertyNames(updateFeedbackDTO));
        double overallRating = (
                updateFeedbackDTO.getProfessionalismRating() +
                        updateFeedbackDTO.getCommunicationRating() +
                        updateFeedbackDTO.getTimelineRating() +
                        updateFeedbackDTO.getContentQualityRating()) / 4.0;
        BigDecimal bd = BigDecimal.valueOf(overallRating).setScale(2, RoundingMode.HALF_UP);
        Double finalRating = bd.doubleValue();
        feedback.setOverallRating(finalRating);
        feedback.setUpdatedAt(Instant.now());
        kolFeedbackRepository.save(feedback);

        FeedbackDTO feedbackDTO = kolFeedbackMapper.toDto(feedback);
        feedbackDTO.setKolId(feedback.getKol().getId());
        feedbackDTO.setReviewerUserId(feedback.getReviewerUser().getId());

        return ApiResponse.<FeedbackDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Update feedback successfully!"))
                .data(feedbackDTO)
                .build();
    }

    @Override
    public ApiResponse<FeedbackDTO> hideOrShowFeedback(UUID feedbackId, boolean isPublic) {
        KolFeedback feedback = kolFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("Feedback not found with ID: " + feedbackId));
        feedback.setIsPublic(isPublic);
        kolFeedbackRepository.save(feedback);
        return ApiResponse.<FeedbackDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Update public status of feedback successfully!"))
                .data(kolFeedbackMapper.toDto(feedback))
                .build();
    }

    private void validateRelationship(User user, Contract contract) {
        if (!contract.getStatus().equalsIgnoreCase(Enums.ContractStatus.COMPLETED.name())) {
            throw new IllegalArgumentException("Contract is not completed! " + contract.getId());
        }
        if (contract.getKolFeedbacks() != null && !contract.getKolFeedbacks().isEmpty()) {
            throw new IllegalArgumentException("Contract already has feedback! " + contract.getId());
        }
        if (!contract.getBookingRequest().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Contract does not belong to this user! " + contract.getId());
        }
    }
}
