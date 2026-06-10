package com.wissen.engine.repository;

import com.wissen.engine.domain.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
    Page<ServiceRequest> findByState(ServiceRequest.RequestState state, Pageable pageable);
    Page<ServiceRequest> findByPriority(ServiceRequest.Priority priority, Pageable pageable);
    Page<ServiceRequest> findByStateAndPriority(ServiceRequest.RequestState state, ServiceRequest.Priority priority, Pageable pageable);
}
