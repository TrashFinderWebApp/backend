package org.example.domain.notification.repository;

import java.util.List;
import org.example.domain.notification.domain.Notification;
import org.example.domain.notification.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Notification> findByStateOrderByCreatedAtDesc(String state, Pageable pageable);
}
