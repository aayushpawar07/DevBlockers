package com.devblocker.notification.controller;

import com.devblocker.notification.dto.NotificationResponse;
import com.devblocker.notification.dto.PageResponse;
import com.devblocker.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    @Operation(summary = "Get user notifications", 
               description = "Retrieves notifications for the authenticated user with pagination")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(value = "userId") UUID userId,
            @RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        PageResponse<NotificationResponse> response = notificationService.getUserNotifications(
                userId, unreadOnly, page, size);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{notificationId}/mark-read")
    @Operation(summary = "Mark notification as read", 
               description = "Marks a specific notification as read for the user")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID notificationId,
            @RequestParam(value = "userId") UUID userId) {
        
        NotificationResponse response = notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count", 
               description = "Returns the count of unread notifications for a user")
    public ResponseEntity<Long> getUnreadCount(
            @RequestParam(value = "userId") UUID userId) {
        
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @PostMapping("/test")
    @Operation(summary = "Create test notification (for testing)", 
               description = "Creates a test notification for a user")
    public ResponseEntity<NotificationResponse> createTestNotification(
            @RequestParam(value = "userId") UUID userId,
            @RequestParam(value = "title", defaultValue = "Test Notification") String title,
            @RequestParam(value = "message", defaultValue = "This is a test notification") String message) {
        
        com.devblocker.notification.model.Notification notification = 
            notificationService.createNotification(
                userId,
                com.devblocker.notification.model.Notification.NotificationType.BLOCKER_CREATED,
                title,
                message,
                null,
                "test"
            );
        
        NotificationResponse response = NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
        
        return ResponseEntity.ok(response);
    }
}

