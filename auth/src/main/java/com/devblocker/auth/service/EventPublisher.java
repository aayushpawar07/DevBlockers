package com.devblocker.auth.service;

import com.devblocker.auth.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${spring.application.name:auth}")
    private String applicationName;
    
    public void publishUserRegistered(User user) {
        try {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .userId(user.getUserId().toString())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .createdAt(user.getCreatedAt())
                    .build();
            
            rabbitTemplate.convertAndSend("user.events", "user.registered", event);
            log.info("Published UserRegistered event for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish UserRegistered event for user: {}", user.getEmail(), e);
            // Don't throw - event publishing failure shouldn't break registration
        }
    }
}

