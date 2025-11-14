package com.web.bookingKol.domain.user.services.impl;


import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.BookingRequestParticipant;
import com.web.bookingKol.domain.booking.models.Campaign;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestParticipantRepository;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.user.dtos.KolInfo;
import com.web.bookingKol.domain.user.repositories.CampaignRepository;
import com.web.bookingKol.domain.user.services.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final ContractRepository contractRepository;
    private final BookingRequestParticipantRepository bookingRequestParticipantRepository;

    @Override
    public ApiResponse<?> getCampaignDetail(UUID id) {
        var campaignOpt = campaignRepository.findById(id);
        if (campaignOpt.isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(List.of("Không tìm thấy campaign với ID: " + id))
                    .data(null)
                    .build();
        }

        Campaign campaign = campaignOpt.get();

        List<BookingRequest> bookingRequests = bookingRequestRepository.findByCampaign_Id(id);

        List<Contract> contracts = contractRepository.findAll().stream()
                .filter(c -> bookingRequests.stream()
                        .anyMatch(br -> br.getId().equals(c.getBookingRequest().getId())))
                .collect(Collectors.toList());

        Set<KolInfo> allKols = new HashSet<>();
        Set<KolInfo> allLives = new HashSet<>();

        for (BookingRequest br : bookingRequests) {
            List<BookingRequestParticipant> participants =
                    bookingRequestParticipantRepository.findByBookingRequest_Id(br.getId());

            for (BookingRequestParticipant p : participants) {
                if (p.getRole() == Enums.BookingParticipantRole.KOL) {
                    allKols.add(KolInfo.builder()
                            .id(p.getKol().getId())
                            .displayName(p.getKol().getDisplayName())
                            .build());
                } else if (p.getRole() == Enums.BookingParticipantRole.LIVE) {
                    allLives.add(KolInfo.builder()
                            .id(p.getKol().getId())
                            .displayName(p.getKol().getDisplayName())
                            .build());
                }
            }
        }

        var data = new LinkedHashMap<String, Object>();
        data.put("campaignId", campaign.getId());
        data.put("name", campaign.getName());
        data.put("objective", campaign.getObjective());
        data.put("targetPrice", campaign.getBudgetMax());
        data.put("startDate", campaign.getStartDate());
        data.put("endDate", campaign.getEndDate());
        data.put("status", campaign.getStatus());
        data.put("createdBy", campaign.getCreatedBy() != null ? campaign.getCreatedBy().getEmail() : null);
        data.put("createdAt", campaign.getCreatedAt());
        data.put("updatedAt", campaign.getUpdatedAt());
        data.put("totalBookingRequests", bookingRequests.size());

        data.put("totalKols", allKols.size());
        data.put("totalLives", allLives.size());
        data.put("kols", allKols);
        data.put("lives", allLives);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy chi tiết campaign thành công"))
                .data(data)
                .build();
    }

}

