package com.wissen.engine.controller;

import com.wissen.engine.domain.AuditLog;
import com.wissen.engine.domain.ServiceRequest;
import com.wissen.engine.dto.*;
import com.wissen.engine.service.AuditService;
import com.wissen.engine.service.AssignmentStrategy;
import com.wissen.engine.service.ServiceRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/requests")
@Slf4j
public class ServiceRequestController {
    @Autowired
    private ServiceRequestService requestService;

    @Autowired
    private AuditService auditService;

    @PostMapping
    public ResponseEntity<ServiceRequestDTO> submitRequest(@RequestBody CreateRequestDTO dto) {
        ServiceRequest request = ServiceRequest.builder()
            .type(dto.getType())
            .priority(dto.getPriority())
            .sensitivity(dto.getSensitivity())
            .title(dto.getTitle())
            .description(dto.getDescription())
            .requesterEmail(dto.getRequesterEmail())
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(requestService.submitRequest(request), false));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequestDTO> getRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToDTO(requestService.getRequest(id), true));
    }

    @GetMapping
    public ResponseEntity<Page<ServiceRequestDTO>> listRequests(
            @RequestParam(required = false) ServiceRequest.RequestState state,
            @RequestParam(required = false) ServiceRequest.Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        Page<ServiceRequest> requests;

        if (state != null && priority != null) requests = requestService.getRequestsByStateAndPriority(state, priority, pageable);
        else if (state != null) requests = requestService.getRequestsByState(state, pageable);
        else if (priority != null) requests = requestService.getRequestsByPriority(priority, pageable);
        else requests = requestService.getAllRequests(pageable);

        return ResponseEntity.ok(requests.map(r -> mapToDTO(r, false)));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ServiceRequestDTO> assignRequest(@PathVariable UUID id, @RequestBody Map<String,String> body) {
        return ResponseEntity.ok(mapToDTO(requestService.assignToOperator(id, body.get("operatorId")), false));
    }

    @PostMapping("/{id}/auto-assign")
    public ResponseEntity<ServiceRequestDTO> autoAssignRequest(@PathVariable UUID id, @RequestBody AssignmentStrategyDTO dto) {
        return ResponseEntity.ok(mapToDTO(requestService.autoAssign(id, dto.getStrategy()), false));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ServiceRequestDTO> completeWork(@PathVariable UUID id, @RequestBody Map<String,String> body) {
        return ResponseEntity.ok(mapToDTO(requestService.completeWork(id, body.get("resolution")), false));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ServiceRequestDTO> approveOrReject(@PathVariable UUID id, @RequestBody ApprovalDTO dto) {
        return ResponseEntity.ok(mapToDTO(requestService.approveOrReject(id, dto.getApproved(), dto.getApproverEmail(), dto.getComments()), false));
    }

    private ServiceRequestDTO mapToDTO(ServiceRequest request, boolean includeAudit) {
        ServiceRequestDTO dto = ServiceRequestDTO.builder()
            .id(request.getId())
            .type(request.getType())
            .priority(request.getPriority())
            .sensitivity(request.getSensitivity())
            .title(request.getTitle())
            .description(request.getDescription())
            .requesterEmail(request.getRequesterEmail())
            .state(request.getState())
            .currentOperatorId(request.getCurrentOperatorId())
            .createdAt(request.getCreatedAt())
            .stateChangedAt(request.getStateChangedAt())
            .assignedAt(request.getAssignedAt())
            .completedAt(request.getCompletedAt())
            .approvedAt(request.getApprovedAt())
            .approvalNeeded(request.getApprovalNeeded())
            .approved(request.getApproved())
            .build();

        if (includeAudit) {
            List<AuditLog> trail = auditService.getAuditTrail(request.getId());
            dto.setAuditTrail(trail.stream().map(log -> AuditLogDTO.builder()
                .id(log.getId())
                .requestId(log.getRequestId())
                .action(log.getAction())
                .actor(log.getActor())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build()).collect(Collectors.toList()));
        }
        return dto;
    }
}
