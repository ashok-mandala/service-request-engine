package com.wissen.engine.domain;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", columnDefinition = "BINARY(16)")
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationEventType eventType;

    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(length = 2000)
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public enum NotificationEventType {
        SUBMITTED, ASSIGNED, COMPLETED, REVIEW_REQUESTED, APPROVED, REJECTED, ESCALATED, OPERATOR_UNAVAILABLE
    }

    public enum NotificationChannel {
        EMAIL, SLACK, SMS, SYSTEM
    }

    public enum NotificationStatus {
        PENDING, SENT, FAILED, ACKNOWLEDGED
    }
}
