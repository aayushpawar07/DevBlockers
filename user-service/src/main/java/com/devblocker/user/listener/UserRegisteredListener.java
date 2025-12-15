package com.devblocker.user.listener;

import com.devblocker.user.service.ProfileService;
import com.devblocker.user.service.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredListener {
    
    private final ProfileService profileService;
    
    @RabbitListener(queues = "user.registered.queue")
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            UUID userId = UUID.fromString(event.getUserId());
            log.info("Received UserRegistered event for user: {}", userId);
            
            profileService.createProfile(userId, event.getEmail());
            log.info("Profile created successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to process UserRegistered event: {}", event, e);
            // Consider implementing retry logic or dead letter queue
        }
    }
}

