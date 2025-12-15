package com.devblocker.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_read", columnList = "is_read"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_user_read", columnList = "user_id, is_read")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id", updatable = false, nullable = false)
    private UUID notificationId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId; // User who should receive this notification
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "related_entity_id")
    private String relatedEntityId; // ID of related blocker/solution/comment
    
    @Column(name = "related_entity_type")
    private String relatedEntityType; // blocker, solution, comment
    
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;
    
    @Column(name = "email_sent", nullable = false)
    @Builder.Default
    private Boolean emailSent = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (read == null) {
            read = false;
        }
        if (emailSent == null) {
            emailSent = false;
        }
    }
    
    public enum NotificationType {
        BLOCKER_CREATED,
        COMMENT_ADDED,
        SOLUTION_ADDED,
        SOLUTION_ACCEPTED,
        SOLUTION_UPVOTED,
        USER_MENTIONED,
        BLOCKER_RESOLVED,
        BLOCKER_UPDATED
    }
}

