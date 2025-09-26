package com.web.bookingKol.domain.file.dtos;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class FileUsageDTO {
    private UUID id;
    private UUID fileId;
    private UUID targetId;
    private String targetType;
    private Boolean isCover;
    private Instant createdAt;

    private FileDTO file;
}
