package com.web.bookingKol.domain.kol.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.bookingKol.domain.user.dtos.NotificationContent;
import com.web.bookingKol.domain.user.dtos.NotificationDTO;
import com.web.bookingKol.domain.user.services.impl.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    @Autowired
    public NotificationServiceImpl(@Qualifier("commonRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                   WebSocketService webSocketService,
                                   ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }

    private String generateRedisKey(String role, UUID userId) {
        if ("KOL".equalsIgnoreCase(role)) {
            return "notifications:KOL:" + userId;
        }
        throw new IllegalArgumentException("Unsupported role: " + role);
    }

    public void processNotification(NotificationDTO notification, String role, UUID userId) {
        if (notification == null || role == null || userId == null) {
            throw new IllegalArgumentException("Notification, role, and userId must not be null");
        }
        notification.setRead(false);
        notification.setTimestamp(notification.getTimestamp() == null ? LocalDate.now() : notification.getTimestamp());
        String key = generateRedisKey(role, userId);
        try {
            String json = objectMapper.writeValueAsString(notification);
            redisTemplate.opsForList().leftPush(key, json);
            webSocketService.sendNotification(role, userId, notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting NotificationDTO to JSON", e);
        }
    }

    public List<NotificationDTO> getUnreadNotifications(String role, UUID userId) {
        if (role == null || userId == null) {
            throw new IllegalArgumentException("Role and userId must not be null");
        }
        String key = generateRedisKey(role, userId);
        List<Object> raw = redisTemplate.opsForList().range(key, 0, -1);
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<NotificationDTO> notifications = new ArrayList<>();
        for (Object obj : raw) {
            try {
                NotificationDTO notification = objectMapper.readValue(obj.toString(), NotificationDTO.class);
                notifications.add(notification);
            } catch (JsonProcessingException e) {
                System.out.println("Error parsing notification: " + e.getMessage());
            }
        }
        return notifications;
    }

    public void sendBulkNotifications(String role, List<UUID> userIds, NotificationContent notifications) {
        for (UUID userId : userIds) {
            NotificationDTO userNotification = new NotificationDTO();
            userNotification.setUserId(userId);
            userNotification.setMessage(notifications.getMessage());
            userNotification.setType(notifications.getType());
            userNotification.setTimestamp(LocalDate.now());
            userNotification.setRead(false);
            processNotification(userNotification, role, userId);
        }
    }

    public void clearNotifications(String role, UUID userId) {
        String key = generateRedisKey(role, userId);
        redisTemplate.delete(key);
    }

    public void markAllAsRead(String role, UUID userId) {
        if (role == null || userId == null) {
            throw new IllegalArgumentException("Role and userId must not be null");
        }
        String key = generateRedisKey(role, userId);
        List<Object> rawNotifications = redisTemplate.opsForList().range(key, 0, -1);
        if (rawNotifications == null || rawNotifications.isEmpty()) {
            return;
        }
        for (int i = 0; i < rawNotifications.size(); i++) {
            Object obj = rawNotifications.get(i);
            try {
                String json = obj.toString();
                NotificationDTO notification;
                if (json.trim().startsWith("[")) {
                    List<NotificationDTO> notifications = objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<NotificationDTO>>() {
                    });
                    if (notifications.isEmpty()) {
                        continue;
                    }
                    notification = notifications.get(0);
                } else {
                    notification = objectMapper.readValue(json, NotificationDTO.class);
                }
                if (!notification.isRead()) {
                    notification.setRead(true);
                    String updatedJson = objectMapper.writeValueAsString(notification);
                    redisTemplate.opsForList().set(key, i, updatedJson);
                }
            } catch (JsonProcessingException e) {
                System.out.println("Error processing notification: " + e.getMessage());
            }
        }
    }
}