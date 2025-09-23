package com.web.bookingKol.domain.kol.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class FilterKolDTO {
    private Double minRating;
    private UUID categoryId;
    private Double minBookingPrice;
    private String city;
}
