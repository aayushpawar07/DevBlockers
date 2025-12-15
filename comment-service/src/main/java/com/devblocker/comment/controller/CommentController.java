package com.devblocker.comment.controller;

import com.devblocker.comment.dto.CommentResponse;
import com.devblocker.comment.dto.CreateCommentRequest;
import com.devblocker.comment.dto.ReplyRequest;
import com.devblocker.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment management endpoints for blockers")
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping("/blockers/{blockerId}/comments")
    @Operation(summary = "Add comment to blocker", 
               description = "Creates a new top-level comment for a blocker and publishes CommentAdded event")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID blockerId,
            @Valid @RequestBody CreateCommentRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String token = extractToken(authHeader);
        CommentResponse response = commentService.addComment(blockerId, request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/blockers/{blockerId}/comments")
    @Operation(summary = "Get comments for blocker", 
               description = "Retrieves all top-level comments for a blocker with threaded replies")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID blockerId) {
        
        List<CommentResponse> comments = commentService.getCommentsByBlocker(blockerId);
        return ResponseEntity.ok(comments);
    }
    
    @PostMapping("/comments/{commentId}/reply")
    @Operation(summary = "Reply to comment", 
               description = "Creates a reply to an existing comment (threaded). Publishes CommentAdded event")
    public ResponseEntity<CommentResponse> replyToComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody ReplyRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String token = extractToken(authHeader);
        CommentResponse response = commentService.replyToComment(commentId, request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/comments/{commentId}")
    @Operation(summary = "Get comment by ID", 
               description = "Retrieves a specific comment with its replies")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable UUID commentId) {
        
        CommentResponse response = commentService.getComment(commentId);
        return ResponseEntity.ok(response);
    }
    
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

