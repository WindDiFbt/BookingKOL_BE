package com.web.bookingKol.domain.user.dtos;

import com.web.bookingKol.config.RabbitMQConfig;
import com.web.bookingKol.domain.user.services.impl.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    @Autowired
    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.CUSTOMER_QUEUE)
    public void receiveCustomerNotification(NotificationDTO notification) {
        if (notification.getUserId() == null) {
            notificationService.processNotification(notification, "CUSTOMER", notification.getUserId());
        } else {
            System.out.println("Not found notifications for customer " + notification.getUserId());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CLEANER_QUEUE)
    public void receiveCleanerNotification(NotificationDTO notification) {
        if (notification.getUserId() == null) {
            notificationService.processNotification(notification, "CLEANER",  notification.getUserId());
        } else {
            System.out.println("Not found notifications for employee " + notification.getUserId());
        }
    }
}