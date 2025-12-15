package com.devblocker.notification.listener;

import com.devblocker.notification.client.BlockerServiceClient;
import com.devblocker.notification.config.RabbitMQConfig;
import com.devblocker.notification.listener.event.SolutionAddedEvent;
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
public class SolutionAddedListener {
    
    private final NotificationService notificationService;
    private final BlockerServiceClient blockerServiceClient;
    
    @RabbitListener(queues = RabbitMQConfig.SOLUTION_ADDED_QUEUE)
    public void handleSolutionAdded(SolutionAddedEvent event) {
        try {
            UUID blockerId = UUID.fromString(event.getBlockerId());
            UUID solutionUserId = UUID.fromString(event.getUserId());
            
            log.info("Received SolutionAdded event for blocker: {}, solution: {}", blockerId, event.getSolutionId());
            
            // Fetch blocker details to get creator/assignee
            BlockerServiceClient.BlockerResponse blocker = blockerServiceClient.getBlocker(blockerId, null);
            
            if (blocker == null) {
                log.warn("Blocker {} not found, cannot create notification for solution", blockerId);
                return;
            }
            
            // Notify blocker creator about new solution (if different from solution author)
            if (blocker.getCreatedBy() != null && !blocker.getCreatedBy().equals(solutionUserId)) {
                notificationService.createNotification(
                        blocker.getCreatedBy(),
                        Notification.NotificationType.SOLUTION_ADDED,
                        "New Solution for Blocker",
                        String.format("A new solution was added for blocker '%s'", blocker.getTitle()),
                        event.getBlockerId(),
                        "blocker"
                );
                log.info("Notification created for blocker creator: {}", blocker.getCreatedBy());
            }
            
            // Notify blocker assignee about new solution (if different from solution author and creator)
            if (blocker.getAssignedTo() != null 
                    && !blocker.getAssignedTo().equals(solutionUserId)
                    && !blocker.getAssignedTo().equals(blocker.getCreatedBy())) {
                notificationService.createNotification(
                        blocker.getAssignedTo(),
                        Notification.NotificationType.SOLUTION_ADDED,
                        "New Solution for Blocker",
                        String.format("A new solution was added for blocker '%s'", blocker.getTitle()),
                        event.getBlockerId(),
                        "blocker"
                );
                log.info("Notification created for blocker assignee: {}", blocker.getAssignedTo());
            }
            
        } catch (Exception e) {
            log.error("Failed to process SolutionAdded event: {}", event, e);
        }
    }
}

