package com.wissen.engine.domain;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(nullable = false)
    private Boolean sensitivity = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestState state;

    @Column(length = 500)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String requesterEmail;

    @Column(name = "current_operator_id")
    private String currentOperatorId;

    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Column(length = 2000)
    private String resolution;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime stateChangedAt;

    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private LocalDateTime approvedAt;

    @Column(nullable = false)
    private Boolean approvalNeeded = false;

    @Column(nullable = false)
    private Boolean approved = false;

    @Column(nullable = false)
    private Integer escalationCount = 0;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (stateChangedAt == null) stateChangedAt = LocalDateTime.now();
    }

    public enum RequestType { INCIDENT, REQUEST, CHANGE }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum RequestState { SUBMITTED, TRIAGE, ASSIGN, WORK, REVIEW, COMPLETE, REJECTED }
}
