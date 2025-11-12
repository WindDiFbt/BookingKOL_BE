package com.web.bookingKol.domain.user.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationContent {

    private String message;
    private String type;
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

}