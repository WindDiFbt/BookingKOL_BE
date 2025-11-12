package com.web.bookingKol.domain.user.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkNotificationRequest {
    private List<UUID> userIds;
    private NotificationContent notification;

    public List<UUID> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }

    public NotificationContent getNotification() {
        return notification;
    }

    public void setNotification(NotificationContent notification) {
        this.notification = notification;
    }
}