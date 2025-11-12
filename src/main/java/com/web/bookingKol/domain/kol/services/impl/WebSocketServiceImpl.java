package com.web.bookingKol.domain.kol.services.impl;

import com.web.bookingKol.domain.user.dtos.NotificationDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WebSocketServiceImpl {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketServiceImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void sendNotification(String role, UUID userId, NotificationDTO notification) {
        String destination = "/topic/kols/" + userId + "/notifications";
        simpMessagingTemplate.convertAndSend(destination, notification);
    }

    public void sendNotificationToMultipleUsers(String role, List<UUID> userIds, NotificationDTO notification) {
        for (UUID id : userIds) {
            sendNotification(role, id, notification);
        }
    }
}