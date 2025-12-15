package com.devblocker.comment.service;

import com.devblocker.comment.config.RabbitMQConfig;
import com.devblocker.comment.model.Comment;
import com.devblocker.comment.service.event.CommentAddedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishCommentAdded(Comment comment) {
        try {
            CommentAddedEvent event = CommentAddedEvent.builder()
                    .commentId(comment.getCommentId().toString())
                    .blockerId(comment.getBlockerId().toString())
                    .userId(comment.getUserId().toString())
                    .parentCommentId(comment.getParentCommentId() != null 
                            ? comment.getParentCommentId().toString() 
                            : null)
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .build();
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.COMMENT_ADDED_ROUTING_KEY,
                    event
            );
            log.info("Published CommentAdded event for comment: {}", comment.getCommentId());
        } catch (Exception e) {
            log.error("Failed to publish CommentAdded event for comment: {}", 
                    comment.getCommentId(), e);
        }
    }
}

