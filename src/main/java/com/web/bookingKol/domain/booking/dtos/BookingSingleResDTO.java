package com.web.bookingKol.domain.booking.dtos;

import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import com.web.bookingKol.domain.kol.models.KolWorkTimeDTO;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class BookingSingleResDTO {
    private UUID id;
    private UUID campaignId;
    private UUID kolId;
    private UUID userId;
    private UUID kolPromoId;
    private String bookingType;
    private String status;
    private String description;
    private String location;

    private Instant startAt;
    private Instant endAt;
    private Boolean isConfirmWithTerms;

    private Instant createdAt;
    private Instant updatedAt;

    private Set<FileUsageDTO> attachedFiles;
    private Set<ContractDTO> contracts;
    private Set<KolWorkTimeDTO> kolWorkTimes;
}
