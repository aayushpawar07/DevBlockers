package com.devblocker.notification.dto;

import com.devblocker.notification.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID notificationId;
    private UUID userId;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private String relatedEntityId;
    private String relatedEntityType;
    private Boolean read;
    private LocalDateTime createdAt;
}

