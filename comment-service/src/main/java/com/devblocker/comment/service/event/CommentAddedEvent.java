package com.devblocker.comment.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentAddedEvent {
    private String commentId;
    private String blockerId;
    private String userId;
    private String parentCommentId; // null for top-level comments
    private String content;
    private LocalDateTime createdAt;
}

