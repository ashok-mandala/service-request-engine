package com.wissen.engine.dto;

import com.wissen.engine.domain.ServiceRequest;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestDTO {
    private UUID id;
    private ServiceRequest.RequestType type;
    private ServiceRequest.Priority priority;
    private Boolean sensitivity;
    private String title;
    private String description;
    private String requesterEmail;
    private ServiceRequest.RequestState state;
    private String currentOperatorId;
    private LocalDateTime createdAt;
    private LocalDateTime stateChangedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private LocalDateTime approvedAt;
    private Boolean approvalNeeded;
    private Boolean approved;
    private List<AuditLogDTO> auditTrail;
}
