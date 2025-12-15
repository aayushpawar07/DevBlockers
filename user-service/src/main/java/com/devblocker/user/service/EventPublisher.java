package com.devblocker.user.service;

import com.devblocker.user.model.Profile;
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
    
    public void publishUserUpdated(UUID userId, Profile profile) {
        try {
            UserUpdatedEvent event = UserUpdatedEvent.builder()
                    .userId(userId.toString())
                    .name(profile.getName())
                    .avatarUrl(profile.getAvatarUrl())
                    .teamId(profile.getTeamId() != null ? profile.getTeamId().toString() : null)
                    .updatedAt(profile.getUpdatedAt())
                    .build();
            
            rabbitTemplate.convertAndSend("user.events", "user.updated", event);
            log.info("Published UserUpdated event for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to publish UserUpdated event for user: {}", userId, e);
            // Don't throw - event publishing failure shouldn't break profile update
        }
    }
}

