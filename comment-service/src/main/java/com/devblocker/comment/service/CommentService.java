package com.devblocker.comment.service;

import com.devblocker.comment.dto.CommentResponse;
import com.devblocker.comment.dto.CreateCommentRequest;
import com.devblocker.comment.dto.ReplyRequest;
import com.devblocker.comment.model.Comment;
import com.devblocker.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public CommentResponse addComment(UUID blockerId, CreateCommentRequest request, String authToken) {
        // Validate blocker exists (optional - could call blocker-service)
        // For now, we'll just create the comment
        
        Comment comment = Comment.builder()
                .blockerId(blockerId)
                .userId(request.getUserId())
                .parentCommentId(null) // Top-level comment
                .content(request.getContent())
                .build();
        
        final Comment savedComment = commentRepository.save(comment);
        
        // Publish event after transaction commit
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishCommentAdded(savedComment);
                    }
                }
        );
        
        log.info("Comment created: {} for blocker: {}", savedComment.getCommentId(), blockerId);
        
        return mapToResponse(savedComment, new ArrayList<>());
    }
    
    public List<CommentResponse> getCommentsByBlocker(UUID blockerId) {
        // Get all top-level comments
        List<Comment> topLevelComments = commentRepository
                .findByBlockerIdAndParentCommentIdIsNullOrderByCreatedAtAsc(blockerId);
        
        // Get all comments for this blocker (including replies)
        List<Comment> allComments = commentRepository.findByBlockerIdOrderByCreatedAtAsc(blockerId);
        
        // Build a map of comment ID to replies
        Map<UUID, List<Comment>> repliesMap = allComments.stream()
                .filter(c -> c.getParentCommentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentCommentId));
        
        // Build threaded response
        return topLevelComments.stream()
                .map(comment -> {
                    List<Comment> replies = repliesMap.getOrDefault(comment.getCommentId(), new ArrayList<>());
                    return mapToResponse(comment, replies);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CommentResponse replyToComment(UUID commentId, ReplyRequest request, String authToken) {
        // Validate parent comment exists
        Comment parentComment = commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found: " + commentId));
        
        Comment reply = Comment.builder()
                .blockerId(parentComment.getBlockerId()) // Reply belongs to same blocker
                .userId(request.getUserId())
                .parentCommentId(commentId) // Set parent comment ID
                .content(request.getContent())
                .build();
        
        final Comment savedReply = commentRepository.save(reply);
        
        // Publish event after transaction commit
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishCommentAdded(savedReply);
                    }
                }
        );
        
        log.info("Reply created: {} to comment: {}", savedReply.getCommentId(), commentId);
        
        return mapToResponse(savedReply, new ArrayList<>());
    }
    
    public CommentResponse getComment(UUID commentId) {
        Comment comment = commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        
        // Get replies to this comment
        List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
        
        return mapToResponse(comment, replies);
    }
    
    /**
     * Maps a comment and its replies to CommentResponse
     * Recursively builds the thread structure
     */
    private CommentResponse mapToResponse(Comment comment, List<Comment> directReplies) {
        // Recursively map replies
        List<CommentResponse> replyResponses = directReplies.stream()
                .map(reply -> {
                    // Get nested replies for this reply
                    List<Comment> nestedReplies = commentRepository
                            .findByParentCommentIdOrderByCreatedAtAsc(reply.getCommentId());
                    return mapToResponse(reply, nestedReplies);
                })
                .collect(Collectors.toList());
        
        // Get reply count
        Integer replyCount = (int) commentRepository.countByParentCommentId(comment.getCommentId());
        
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .blockerId(comment.getBlockerId())
                .userId(comment.getUserId())
                .parentCommentId(comment.getParentCommentId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replies(replyResponses)
                .replyCount(replyCount)
                .build();
    }
}

