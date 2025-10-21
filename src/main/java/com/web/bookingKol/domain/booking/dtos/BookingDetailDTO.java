package com.web.bookingKol.domain.booking.dtos;

import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import com.web.bookingKol.domain.kol.dtos.KolDetailDTO;
import com.web.bookingKol.domain.user.dtos.UserDTO;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class BookingDetailDTO {
    private UUID id;
    private UUID campaignId;
    private KolDetailDTO kol;
    private UserDTO user;
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
}
