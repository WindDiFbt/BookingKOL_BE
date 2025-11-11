package com.web.bookingKol.domain.user.dtos;

import com.web.bookingKol.domain.booking.dtos.ContractPaymentScheduleResponse;
import com.web.bookingKol.domain.booking.dtos.KolParticipantResponse;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class UserBookingRequestResponse {
    private UUID bookingRequestId;
    private String requestNumber;
    private String description;
    private String status;
    private String repeatType;
    private String dayOfWeek;
    private LocalDate repeatUntil;
    private BigDecimal contractAmount;
    private Instant createdAt;
    private Instant updatedAt;

    private UUID campaignId;
    private String campaignName;
    private Instant campaignStartDate;
    private Instant campaignEndDate;

    private UUID contractId;
    private String contractNumber;
    private String contractStatus;
    private String contractTerms;
    private String contractFileUrl;

    private List<KolParticipantResponse> kolParticipants;
    private List<KolParticipantResponse> liveParticipants;
    private List<ContractPaymentScheduleResponse> paymentSchedules;

    public static UserBookingRequestResponse from(BookingRequest br, Contract contract) {
        var campaign = br.getCampaign();

        return UserBookingRequestResponse.builder()
                .bookingRequestId(br.getId())
                .requestNumber(br.getRequestNumber())
                .description(br.getDescription())
                .status(br.getStatus())
                .repeatType(br.getRepeatType())
                .dayOfWeek(br.getDayOfWeek())
                .repeatUntil(br.getRepeatUntil())
                .contractAmount(br.getContractAmount())
                .createdAt(br.getCreatedAt())
                .updatedAt(br.getUpdatedAt())

                .campaignId(campaign != null ? campaign.getId() : null)
                .campaignName(campaign != null ? campaign.getName() : null)
                .campaignStartDate(campaign != null ? campaign.getStartDate() : null)
                .campaignEndDate(campaign != null ? campaign.getEndDate() : null)

                .contractId(contract != null ? contract.getId() : null)
                .contractNumber(contract != null ? contract.getContractNumber() : null)
                .contractStatus(contract != null ? contract.getStatus() : null)
                .contractTerms(contract != null ? contract.getTerms() : null)
                .contractFileUrl(contract != null ? extractFileUrl(contract.getTerms()) : null)
                .paymentSchedules(List.of())
                .build();
    }

    public static UserBookingRequestResponse from(
            BookingRequest br,
            Contract contract,
            List<KolParticipantResponse> kolParticipants,
            List<KolParticipantResponse> liveParticipants
    ) {
        return from(br, contract).toBuilder()
                .kolParticipants(kolParticipants)
                .liveParticipants(liveParticipants)
                .build();
    }

    private static String extractFileUrl(String terms) {
        if (terms == null) return null;
        return terms.contains("uploads/") ? terms.substring(terms.indexOf("uploads/")).trim() : terms;
    }
}
