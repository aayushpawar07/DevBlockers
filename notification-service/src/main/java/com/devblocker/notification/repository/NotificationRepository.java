package com.devblocker.notification.repository;

import com.devblocker.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    /**
     * Find all notifications for a user, ordered by creation date (newest first)
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Find unread notifications for a user
     */
    Page<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndReadFalse(UUID userId);
    
    /**
     * Find notification by ID and user ID (for security)
     */
    Notification findByNotificationIdAndUserId(UUID notificationId, UUID userId);
}

