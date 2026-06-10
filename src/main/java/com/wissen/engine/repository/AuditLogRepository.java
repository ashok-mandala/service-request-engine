package com.wissen.engine.repository;

import com.wissen.engine.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByRequestIdOrderByTimestampAsc(UUID requestId);
    List<AuditLog> findByActorOrderByTimestampDesc(String actor);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
