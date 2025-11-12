package com.web.bookingKol.domain.user.services.impl;

import com.web.bookingKol.domain.user.dtos.NotificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WebSocketService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void sendNotification(String role, UUID userId, NotificationDTO notification) {
        String destination = role.equals("CUSTOMER") ?
                "/topic/customers/" + userId + "/notifications" :
                "/topic/cleaners/" + userId + "/notifications";

        simpMessagingTemplate.convertAndSend(destination, notification);
    }

    // Send to multiple
    public void sendNotificationToMultipleUsers(String role, List<UUID> userIds, NotificationDTO notification) {
        for (UUID userId : userIds) {
            sendNotification(role, userId, notification);
        }
    }

}