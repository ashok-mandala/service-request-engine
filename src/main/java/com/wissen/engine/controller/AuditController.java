package com.wissen.engine.controller;

import com.wissen.engine.domain.AuditLog;
import com.wissen.engine.dto.AuditLogDTO;
import com.wissen.engine.service.AuditService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
@Slf4j
public class AuditController {
    @Autowired
    private AuditService auditService;

    @GetMapping("/{requestId}")
    public ResponseEntity<AuditTrailResponse> getAuditTrail(@PathVariable UUID requestId) {
        List<AuditLog> trail = auditService.getAuditTrail(requestId);
        List<AuditLogDTO> dtos = trail.stream().map(log -> AuditLogDTO.builder()
            .id(log.getId())
            .requestId(log.getRequestId())
            .action(log.getAction())
            .actor(log.getActor())
            .details(log.getDetails())
            .timestamp(log.getTimestamp())
            .metadata(log.getMetadata())
            .build()).collect(Collectors.toList());
        return ResponseEntity.ok(AuditTrailResponse.builder().requestId(requestId).trail(dtos).build());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditTrailResponse {
        private UUID requestId;
        private List<AuditLogDTO> trail;
    }
}
