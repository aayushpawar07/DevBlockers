package com.devblocker.notification.listener;

import com.devblocker.notification.client.UserServiceClient;
import com.devblocker.notification.config.RabbitMQConfig;
import com.devblocker.notification.listener.event.BlockerCreatedEvent;
import com.devblocker.notification.model.Notification;
import com.devblocker.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockerCreatedListener {
    
    private final NotificationService notificationService;
    private final UserServiceClient userServiceClient;
    
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
            
            // Notify team members if teamCode is specified
            if (event.getTeamCode() != null && !event.getTeamCode().isEmpty()) {
                log.info("Notifying team members for blocker {} with teamCode: {}", blockerId, event.getTeamCode());
                
                // Fetch team members from user-service
                List<UUID> teamMemberIds = userServiceClient.getTeamMembersByCode(event.getTeamCode(), null);
                
                if (teamMemberIds != null && !teamMemberIds.isEmpty()) {
                    for (UUID memberId : teamMemberIds) {
                        // Skip creator and assigned user (already notified)
                        if (memberId.equals(createdBy)) {
                            continue;
                        }
                        if (event.getAssignedTo() != null && memberId.toString().equals(event.getAssignedTo())) {
                            continue;
                        }
                        
                        notificationService.createNotification(
                                memberId,
                                Notification.NotificationType.TEAM_BLOCKER_CREATED,
                                "New Team Blocker",
                                String.format("A new blocker '%s' was created in your team (%s)", 
                                        event.getTitle(), event.getTeamCode()),
                                event.getBlockerId(),
                                "blocker"
                        );
                        log.info("Team notification created for team member: {} (team: {})", memberId, event.getTeamCode());
                    }
                } else {
                    log.warn("No team members found for team code: {}", event.getTeamCode());
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to process BlockerCreated event: {}", event, e);
        }
    }
}

