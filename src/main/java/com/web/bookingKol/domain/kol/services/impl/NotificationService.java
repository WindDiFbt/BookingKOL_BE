package com.web.bookingKol.domain.user.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.bookingKol.domain.user.dtos.NotificationContent;
import com.web.bookingKol.domain.user.dtos.NotificationDTO;
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
public class NotificationService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    @Autowired
    public NotificationService(@Qualifier("commonRedisTemplate") RedisTemplate<String, Object> redisTemplate, WebSocketService webSocketService, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }

    public void processNotification(NotificationDTO notification, String role, UUID userId) {
        if (notification == null || role == null || userId == null) {
            throw new IllegalArgumentException("Notification, role, and userId must not be null");
        }

        String key = generateRedisKey(role, userId);
        // Đảm bảo thông báo mới luôn là chưa đọc
        notification.setRead(false);
        try {
            // Convert Object -> JSON String trước khi lưu vào Redis
            String jsonNotification = objectMapper.writeValueAsString(notification);

            // Save JSON string vào Redis thay vì Object
            redisTemplate.opsForList().leftPush(key, jsonNotification);

            // Send to WebSocket
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
        List<Object> rawNotifications = redisTemplate.opsForList().range(key, 0, -1);

        if (rawNotifications == null || rawNotifications.isEmpty()) {
            return Collections.emptyList();
        }

        List<NotificationDTO> notifications = new ArrayList<>();
        for (Object obj : rawNotifications) {
            try {
                NotificationDTO notification = objectMapper.readValue(obj.toString(), NotificationDTO.class);
                notifications.add(notification);
//                if (!notification.isRead()) {
//                    notifications.add(notification);
//                }
            } catch (JsonProcessingException e) {
                // Log lỗi parse nhưng bỏ qua, không throw
                System.out.println("Error parsing notification: " + e.getMessage());
                // Bạn có thể dùng logger thay vì System.out nếu cần production-ready
            }
        }

        return notifications;
    }

    private String generateRedisKey(String role, UUID userId) {
        switch (role.toUpperCase()) {
            case "CUSTOMER":
                return "notifications:customers:" + userId;
            case "CLEANER":
                return "notifications:cleaners:" + userId;
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    public void sendBulkNotifications(String role, List<UUID> userIds, NotificationContent notifications) {
        for (UUID userId : userIds) {
            NotificationDTO userNotification = new NotificationDTO();
            userNotification.setUserId(userId);
            userNotification.setMessage(notifications.getMessage());
            userNotification.setType(notifications.getType());
            userNotification.setTimestamp(LocalDate.now());
            userNotification.setRead(false); // Chắc chắn chưa đọc

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

                // Nếu json bắt đầu bằng "[" thì đọc List
                if (json.trim().startsWith("[")) {
                    List<NotificationDTO> notifications = objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<NotificationDTO>>() {});
                    if (notifications.isEmpty()) {
                        continue;
                    }
                    notification = notifications.get(0);
                } else {
                    notification = objectMapper.readValue(json, NotificationDTO.class);
                }

                // Nếu chưa đọc thì set read = true và update
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