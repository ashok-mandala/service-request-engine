package com.wissen.engine.dto;

import com.wissen.engine.domain.AuditLog;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
    private Long id;
    private UUID requestId;
    private AuditLog.AuditAction action;
    private String actor;
    private String details;
    private LocalDateTime timestamp;
    private String metadata;
}
