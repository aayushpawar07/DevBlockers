package com.devblocker.notification.listener;

import com.devblocker.notification.config.RabbitMQConfig;
import com.devblocker.notification.listener.event.SolutionAcceptedEvent;
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
public class SolutionAcceptedListener {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = RabbitMQConfig.SOLUTION_ACCEPTED_QUEUE)
    public void handleSolutionAccepted(SolutionAcceptedEvent event) {
        try {
            UUID blockerId = UUID.fromString(event.getBlockerId());
            UUID solutionUserId = UUID.fromString(event.getUserId());
            UUID acceptedBy = UUID.fromString(event.getAcceptedBy());
            
            log.info("Received SolutionAccepted event for blocker: {}, solution: {}", blockerId, event.getSolutionId());
            
            // Notify the solution author that their solution was accepted
            if (!solutionUserId.equals(acceptedBy)) {
                notificationService.createNotification(
                        solutionUserId,
                        Notification.NotificationType.SOLUTION_ACCEPTED,
                        "Solution Accepted!",
                        String.format("Your solution for blocker has been accepted as the best solution"),
                        event.getBlockerId(),
                        "blocker"
                );
            }
            
        } catch (Exception e) {
            log.error("Failed to process SolutionAccepted event: {}", event, e);
        }
    }
}

