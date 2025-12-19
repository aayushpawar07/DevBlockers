package com.devblocker.blocker.service;

import com.devblocker.blocker.model.Blocker;
import com.devblocker.blocker.service.event.BlockerCreatedEvent;
import com.devblocker.blocker.service.event.BlockerResolvedEvent;
import com.devblocker.blocker.service.event.BlockerUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishBlockerCreated(Blocker blocker) {
        try {
            BlockerCreatedEvent event = BlockerCreatedEvent.builder()
                    .blockerId(blocker.getBlockerId().toString())
                    .title(blocker.getTitle())
                    .description(blocker.getDescription())
                    .status(blocker.getStatus().name())
                    .severity(blocker.getSeverity().name())
                    .createdBy(blocker.getCreatedBy().toString())
                    .assignedTo(blocker.getAssignedTo() != null ? blocker.getAssignedTo().toString() : null)
                    .teamId(blocker.getTeamId() != null ? blocker.getTeamId().toString() : null)
                    .teamCode(null) // Blocker model doesn't have teamCode field
                    .tags(blocker.getTags())
                    .createdAt(blocker.getCreatedAt())
                    .build();
            
            rabbitTemplate.convertAndSend("blocker.events", "blocker.created", event);
            log.info("Published BlockerCreated event for blocker: {}", blocker.getBlockerId());
        } catch (Exception e) {
            log.error("Failed to publish BlockerCreated event for blocker: {}", blocker.getBlockerId(), e);
            // Don't throw - event publishing failure shouldn't break blocker creation
        }
    }
    
    public void publishBlockerUpdated(Blocker blocker) {
        try {
            BlockerUpdatedEvent event = BlockerUpdatedEvent.builder()
                    .blockerId(blocker.getBlockerId().toString())
                    .title(blocker.getTitle())
                    .description(blocker.getDescription())
                    .status(blocker.getStatus().name())
                    .severity(blocker.getSeverity().name())
                    .createdBy(blocker.getCreatedBy().toString())
                    .assignedTo(blocker.getAssignedTo() != null ? blocker.getAssignedTo().toString() : null)
                    .teamId(blocker.getTeamId() != null ? blocker.getTeamId().toString() : null)
                    .tags(blocker.getTags())
                    .updatedAt(blocker.getUpdatedAt())
                    .build();
            
            rabbitTemplate.convertAndSend("blocker.events", "blocker.updated", event);
            log.info("Published BlockerUpdated event for blocker: {}", blocker.getBlockerId());
        } catch (Exception e) {
            log.error("Failed to publish BlockerUpdated event for blocker: {}", blocker.getBlockerId(), e);
        }
    }
    
    public void publishBlockerResolved(Blocker blocker, UUID resolvedBy) {
        try {
            BlockerResolvedEvent event = BlockerResolvedEvent.builder()
                    .blockerId(blocker.getBlockerId().toString())
                    .title(blocker.getTitle())
                    .bestSolutionId(blocker.getBestSolutionId() != null ? blocker.getBestSolutionId().toString() : null)
                    .resolvedBy(resolvedBy != null ? resolvedBy.toString() : null)
                    .resolvedAt(blocker.getResolvedAt())
                    .build();
            
            rabbitTemplate.convertAndSend("blocker.events", "blocker.resolved", event);
            log.info("Published BlockerResolved event for blocker: {}", blocker.getBlockerId());
        } catch (Exception e) {
            log.error("Failed to publish BlockerResolved event for blocker: {}", blocker.getBlockerId(), e);
        }
    }
}

