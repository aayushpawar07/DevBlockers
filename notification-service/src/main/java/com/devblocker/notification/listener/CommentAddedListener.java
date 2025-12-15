package com.devblocker.notification.listener;

import com.devblocker.notification.client.BlockerServiceClient;
import com.devblocker.notification.config.RabbitMQConfig;
import com.devblocker.notification.listener.event.CommentAddedEvent;
import com.devblocker.notification.model.Notification;
import com.devblocker.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentAddedListener {
    
    private final NotificationService notificationService;
    private final BlockerServiceClient blockerServiceClient;
    
    @RabbitListener(queues = RabbitMQConfig.COMMENT_ADDED_QUEUE)
    public void handleCommentAdded(CommentAddedEvent event) {
        try {
            UUID blockerId = UUID.fromString(event.getBlockerId());
            UUID commentUserId = UUID.fromString(event.getUserId());
            
            log.info("Received CommentAdded event for blocker: {}, comment: {}", blockerId, event.getCommentId());
            
            // Fetch blocker details to get creator/assignee
            BlockerServiceClient.BlockerResponse blocker = blockerServiceClient.getBlocker(blockerId, null);
            
            if (blocker == null) {
                log.warn("Blocker {} not found, cannot create notification for comment", blockerId);
                return;
            }
            
            // If this is a reply, notify the parent comment author
            if (event.getParentCommentId() != null && !event.getParentCommentId().isEmpty()) {
                log.debug("Reply to comment {} - parent comment author should be notified", event.getParentCommentId());
                // TODO: Fetch parent comment author from comment-service and notify them
                // For now, we need the parent comment's userId to create a notification
            }
            
            // Notify blocker creator about new comment (if different from comment author)
            if (blocker.getCreatedBy() != null && !blocker.getCreatedBy().equals(commentUserId)) {
                notificationService.createNotification(
                        blocker.getCreatedBy(),
                        Notification.NotificationType.COMMENT_ADDED,
                        "New Comment on Blocker",
                        String.format("A new comment was added to blocker '%s'", blocker.getTitle()),
                        event.getBlockerId(),
                        "blocker"
                );
                log.info("Notification created for blocker creator: {}", blocker.getCreatedBy());
            }
            
            // Notify blocker assignee about new comment (if different from comment author and creator)
            if (blocker.getAssignedTo() != null 
                    && !blocker.getAssignedTo().equals(commentUserId)
                    && !blocker.getAssignedTo().equals(blocker.getCreatedBy())) {
                notificationService.createNotification(
                        blocker.getAssignedTo(),
                        Notification.NotificationType.COMMENT_ADDED,
                        "New Comment on Blocker",
                        String.format("A new comment was added to blocker '%s'", blocker.getTitle()),
                        event.getBlockerId(),
                        "blocker"
                );
                log.info("Notification created for blocker assignee: {}", blocker.getAssignedTo());
            }
            
        } catch (Exception e) {
            log.error("Failed to process CommentAdded event: {}", event, e);
        }
    }
}

