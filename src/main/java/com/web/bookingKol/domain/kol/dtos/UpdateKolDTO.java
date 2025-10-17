package com.web.bookingKol.domain.kol.dtos;

import com.web.bookingKol.common.Enums;
import jakarta.persistence.Column;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class UpdateKolDTO implements Serializable {
    private String fullName;
    private String displayName;
    private Date dob;
    private String phone;
    private String gender;
    private String address;
    private String avatarUrl;
    private String status;
    private String bio;
    private String experience;
    private String country;
    private String city;
    private String languages;
    private String rateCardNote;
    private BigDecimal minBookingPrice;
    private Enums.Roles role;

    private Double overallRating;
    private Integer feedbackCount;
}
