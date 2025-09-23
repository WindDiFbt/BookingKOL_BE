package com.web.bookingKol.domain.kol.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class PlatformDTO {
    private UUID id;
    private String key;
    private String name;
}
