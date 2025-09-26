package com.web.bookingKol.domain.file.dtos;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class FileDTO {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long sizeBytes;
    private Instant createdAt;
    private String status;
}
