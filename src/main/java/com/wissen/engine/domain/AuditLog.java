package com.wissen.engine.domain;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log", indexes = {@Index(name = "idx_request_id", columnList = "request_id")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(nullable = false)
    private String actor;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 500)
    private String metadata;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }

    public enum AuditAction {
        SUBMITTED, TRIAGED, ASSIGNED, REASSIGNED, WORK_STARTED, WORK_COMPLETED,
        REVIEW_REQUESTED, APPROVED, REJECTED, ESCALATED, COMPLETED, OPERATOR_UNAVAILABLE
    }
}
