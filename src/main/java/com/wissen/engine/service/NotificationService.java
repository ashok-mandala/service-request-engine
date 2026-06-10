package com.wissen.engine.service;

import com.wissen.engine.domain.Notification;
import com.wissen.engine.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public Notification notify(UUID requestId, Notification.NotificationEventType eventType, String recipient,
                              Notification.NotificationChannel channel, String message) {
        Notification notification = Notification.builder()
            .requestId(requestId)
            .eventType(eventType)
            .recipient(recipient)
            .channel(channel)
            .message(message)
            .status(Notification.NotificationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
        return sendNotification(notificationRepository.save(notification));
    }

    private Notification sendNotification(Notification notification) {
        try {
            log.info("Sending {} notification to {}: {}", notification.getChannel(), notification.getRecipient(), notification.getMessage());
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            notification.setStatus(Notification.NotificationStatus.FAILED);
        }
        return notificationRepository.save(notification);
    }
}
