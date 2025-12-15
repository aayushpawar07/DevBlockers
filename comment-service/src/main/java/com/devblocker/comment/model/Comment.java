package com.devblocker.comment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_blocker_id", columnList = "blocker_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_parent_comment_id", columnList = "parent_comment_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "comment_id", updatable = false, nullable = false)
    private UUID commentId;
    
    @Column(name = "blocker_id", nullable = false)
    private UUID blockerId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "parent_comment_id")
    private UUID parentCommentId; // null for top-level comments, UUID for replies
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

