package com.devblocker.notification.listener.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentAddedEvent {
    private String commentId;
    private String blockerId;
    private String userId;
    private String parentCommentId;
    private String content;
    private LocalDateTime createdAt;
}

