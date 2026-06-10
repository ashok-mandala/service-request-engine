package com.wissen.engine.service;

import com.wissen.engine.domain.AuditLog;
import com.wissen.engine.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AuditService {
    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog recordAction(UUID requestId, AuditLog.AuditAction action, String actor, String details) {
        return recordAction(requestId, action, actor, details, null);
    }

    public AuditLog recordAction(UUID requestId, AuditLog.AuditAction action, String actor, String details, String metadata) {
        AuditLog entry = AuditLog.builder()
            .requestId(requestId)
            .action(action)
            .actor(actor)
            .details(details)
            .metadata(metadata)
            .timestamp(LocalDateTime.now())
            .build();
        return auditLogRepository.save(entry);
    }

    public List<AuditLog> getAuditTrail(UUID requestId) {
        return auditLogRepository.findByRequestIdOrderByTimestampAsc(requestId);
    }
}
