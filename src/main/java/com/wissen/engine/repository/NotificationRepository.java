package com.wissen.engine.repository;

import com.wissen.engine.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRequestId(UUID requestId);
    List<Notification> findByStatusAndChannel(Notification.NotificationStatus status, Notification.NotificationChannel channel);
}
