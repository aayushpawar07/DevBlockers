package com.devblocker.notification.service;

import com.devblocker.notification.dto.NotificationResponse;
import com.devblocker.notification.dto.PageResponse;
import com.devblocker.notification.model.Notification;
import com.devblocker.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    
    /**
     * Create a new notification
     * Called by event listeners when events are received
     */
    @Transactional
    public Notification createNotification(
            UUID userId,
            Notification.NotificationType type,
            String title,
            String message,
            String relatedEntityId,
            String relatedEntityType) {
        
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .read(false)
                .emailSent(false)
                .build();
        
        notification = notificationRepository.save(notification);
        
        log.info("Notification created: {} for user: {}", notification.getNotificationId(), userId);
        
        // Send email if configured (async in production)
        // For now, we'll just mark it - in production, use async email sending
        if (emailService.shouldSendEmail(type)) {
            // In production, fetch user email from user-service
            // For now, we'll just log it
            log.info("Email notification should be sent for notification: {}", notification.getNotificationId());
        }
        
        return notification;
    }
    
    /**
     * Get notifications for a user
     */
    public PageResponse<NotificationResponse> getUserNotifications(
            UUID userId, 
            boolean unreadOnly, 
            int page, 
            int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications;
        
        if (unreadOnly) {
            notifications = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(
                    userId, pageable);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, pageable);
        }
        
        return PageResponse.<NotificationResponse>builder()
                .content(notifications.getContent().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .page(notifications.getNumber())
                .size(notifications.getSize())
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .first(notifications.isFirst())
                .last(notifications.isLast())
                .build();
    }
    
    /**
     * Mark notification as read
     */
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndUserId(notificationId, userId);
        
        if (notification == null) {
            throw new IllegalArgumentException("Notification not found or access denied");
        }
        
        if (!notification.getRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
            log.info("Notification {} marked as read by user {}", notificationId, userId);
        }
        
        return mapToResponse(notification);
    }
    
    /**
     * Get unread count for a user
     */
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
    
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

