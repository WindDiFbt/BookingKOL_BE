package com.web.bookingKol.domain.kol.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class CategoryDTO {
    private UUID id;
    private String name;
    private String key;
}
