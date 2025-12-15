package com.devblocker.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private UUID commentId;
    private UUID blockerId;
    private UUID userId;
    private UUID parentCommentId; // null for top-level comments
    private String content;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies; // Nested replies for threaded display
    private Integer replyCount; // Number of direct replies
}

