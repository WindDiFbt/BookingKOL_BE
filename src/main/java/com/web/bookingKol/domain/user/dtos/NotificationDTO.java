package com.web.bookingKol.domain.user.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Setter
@Getter
public class NotificationDTO implements Serializable {

    private UUID userId;
    private String message;
    private String type;
    private LocalDate timestamp;
    private boolean read = false;
}