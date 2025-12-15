package com.devblocker.notification.listener;

import com.devblocker.notification.config.RabbitMQConfig;
import com.devblocker.notification.listener.event.BlockerCreatedEvent;
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
public class BlockerCreatedListener {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = RabbitMQConfig.BLOCKER_CREATED_QUEUE)
    public void handleBlockerCreated(BlockerCreatedEvent event) {
        try {
            UUID blockerId = UUID.fromString(event.getBlockerId());
            UUID createdBy = UUID.fromString(event.getCreatedBy());
            
            log.info("Received BlockerCreated event for blocker: {}", blockerId);
            
            // Notify assigned user if different from creator
            if (event.getAssignedTo() != null && !event.getAssignedTo().equals(event.getCreatedBy())) {
                UUID assignedTo = UUID.fromString(event.getAssignedTo());
                
                notificationService.createNotification(
                        assignedTo,
                        Notification.NotificationType.BLOCKER_CREATED,
                        "New Blocker Assigned",
                        String.format("A new blocker '%s' has been assigned to you", event.getTitle()),
                        event.getBlockerId(),
                        "blocker"
                );
                log.info("Notification created for assigned user: {}", assignedTo);
            }
            
            // Always notify the creator (they created it, so they should know)
            notificationService.createNotification(
                    createdBy,
                    Notification.NotificationType.BLOCKER_CREATED,
                    "Blocker Created",
                    String.format("You created a new blocker '%s'", event.getTitle()),
                    event.getBlockerId(),
                    "blocker"
            );
            log.info("Notification created for blocker creator: {}", createdBy);
            
            // Notify team members if team is specified
            // (In production, fetch team members from user-service)
            if (event.getTeamId() != null) {
                log.debug("Team notification for blocker {} - team members should be notified", blockerId);
                // TODO: Fetch team members and notify them
            }
            
        } catch (Exception e) {
            log.error("Failed to process BlockerCreated event: {}", event, e);
        }
    }
}

