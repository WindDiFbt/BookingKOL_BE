package com.web.bookingKol.domain.user.services.impl;


import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.Campaign;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
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
                .filter(c -> bookingRequests.stream().anyMatch(br -> br.getId().equals(c.getBookingRequest().getId())))
                .collect(Collectors.toList());

        var data = new LinkedHashMap<String, Object>();
        data.put("campaignId", campaign.getId());
        data.put("name", campaign.getName());
        data.put("objective", campaign.getObjective());
        data.put("budgetMin", campaign.getBudgetMin());
        data.put("budgetMax", campaign.getBudgetMax());
        data.put("startDate", campaign.getStartDate());
        data.put("endDate", campaign.getEndDate());
        data.put("status", campaign.getStatus());
        data.put("createdBy", campaign.getCreatedBy().getEmail());
        data.put("createdAt", campaign.getCreatedAt());
        data.put("updatedAt", campaign.getUpdatedAt());
        data.put("totalBookingRequests", bookingRequests.size());

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy chi tiết campaign thành công"))
                .data(data)
                .build();
    }
}

