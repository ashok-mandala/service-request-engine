package com.wissen.engine.service;

import com.wissen.engine.domain.AuditLog;
import com.wissen.engine.domain.Notification;
import com.wissen.engine.domain.Operator;
import com.wissen.engine.domain.ServiceRequest;
import com.wissen.engine.repository.ServiceRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class ServiceRequestService {
    @Autowired
    private ServiceRequestRepository requestRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AssignmentService assignmentService;

    @Transactional
    public ServiceRequest submitRequest(ServiceRequest request) {
        if (request.getId() == null) request.setId(UUID.randomUUID());
        request.setState(ServiceRequest.RequestState.SUBMITTED);
        request.setCreatedAt(LocalDateTime.now());
        request.setStateChangedAt(LocalDateTime.now());
        request.setApprovalNeeded(
            request.getPriority() == ServiceRequest.Priority.HIGH ||
            request.getPriority() == ServiceRequest.Priority.CRITICAL ||
            request.getSensitivity()
        );

        ServiceRequest saved = requestRepository.save(request);
        auditService.recordAction(saved.getId(), AuditLog.AuditAction.SUBMITTED, request.getRequesterEmail(), "Request submitted");
        triage(saved);
        return saved;
    }

    @Transactional
    public ServiceRequest triage(ServiceRequest request) {
        request.setState(ServiceRequest.RequestState.TRIAGE);
        request.setStateChangedAt(LocalDateTime.now());
        ServiceRequest saved = requestRepository.save(request);
        auditService.recordAction(saved.getId(), AuditLog.AuditAction.TRIAGED, "system", "Request triaged");
        return saved;
    }

    @Transactional
    public ServiceRequest assignToOperator(UUID requestId, String operatorId) {
        ServiceRequest request = requestRepository.findById(requestId).orElseThrow();
        request.setCurrentOperatorId(operatorId);
        request.setState(ServiceRequest.RequestState.ASSIGN);
        request.setAssignedAt(LocalDateTime.now());
        request.setStateChangedAt(LocalDateTime.now());

        ServiceRequest saved = requestRepository.save(request);
        assignmentService.incrementLoad(operatorId);
        auditService.recordAction(saved.getId(), AuditLog.AuditAction.ASSIGNED, operatorId, "Assigned to operator");

        saved.setState(ServiceRequest.RequestState.WORK);
        saved.setStateChangedAt(LocalDateTime.now());
        requestRepository.save(saved);
        auditService.recordAction(saved.getId(), AuditLog.AuditAction.WORK_STARTED, "system", "Work started");
        return saved;
    }

    @Transactional
    public ServiceRequest autoAssign(UUID requestId, AssignmentStrategy strategy) {
        ServiceRequest request = requestRepository.findById(requestId).orElseThrow();
        Operator operator = assignmentService.assignRequest(request, strategy);
        if (operator == null) throw new RuntimeException("No available operators");
        return assignToOperator(requestId, operator.getId());
    }

    @Transactional
    public ServiceRequest completeWork(UUID requestId, String resolution) {
        ServiceRequest request = requestRepository.findById(requestId).orElseThrow();
        request.setResolution(resolution);
        request.setCompletedAt(LocalDateTime.now());

        if (request.getApprovalNeeded()) {
            request.setState(ServiceRequest.RequestState.REVIEW);
            auditService.recordAction(request.getId(), AuditLog.AuditAction.REVIEW_REQUESTED, request.getCurrentOperatorId(), "Awaiting approval");
        } else {
            request.setState(ServiceRequest.RequestState.COMPLETE);
            auditService.recordAction(request.getId(), AuditLog.AuditAction.COMPLETED, request.getCurrentOperatorId(), "Completed");
        }

        request.setStateChangedAt(LocalDateTime.now());
        ServiceRequest saved = requestRepository.save(request);
        if (request.getCurrentOperatorId() != null) assignmentService.decrementLoad(request.getCurrentOperatorId());
        return saved;
    }

    @Transactional
    public ServiceRequest approveOrReject(UUID requestId, Boolean approved, String approverEmail, String comments) {
        ServiceRequest request = requestRepository.findById(requestId).orElseThrow();

        if (approved) {
            request.setState(ServiceRequest.RequestState.COMPLETE);
            request.setApproved(true);
            request.setApprovedAt(LocalDateTime.now());
            auditService.recordAction(request.getId(), AuditLog.AuditAction.APPROVED, approverEmail, "Approved", comments);
        } else {
            request.setState(ServiceRequest.RequestState.SUBMITTED);
            request.setCurrentOperatorId(null);
            request.setAssignedAt(null);
            auditService.recordAction(request.getId(), AuditLog.AuditAction.REJECTED, approverEmail, "Rejected - rework needed", comments);
        }

        request.setStateChangedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    public ServiceRequest getRequest(UUID id) {
        return requestRepository.findById(id).orElseThrow();
    }

    public Page<ServiceRequest> getRequestsByState(ServiceRequest.RequestState state, Pageable pageable) {
        return requestRepository.findByState(state, pageable);
    }

    public Page<ServiceRequest> getRequestsByPriority(ServiceRequest.Priority priority, Pageable pageable) {
        return requestRepository.findByPriority(priority, pageable);
    }

    public Page<ServiceRequest> getRequestsByStateAndPriority(ServiceRequest.RequestState state, ServiceRequest.Priority priority, Pageable pageable) {
        return requestRepository.findByStateAndPriority(state, priority, pageable);
    }

    public Page<ServiceRequest> getAllRequests(Pageable pageable) {
        return requestRepository.findAll(pageable);
    }
}
