package com.devblocker.solution.service;

import com.devblocker.solution.config.RabbitMQConfig;
import com.devblocker.solution.model.Solution;
import com.devblocker.solution.service.event.SolutionAcceptedEvent;
import com.devblocker.solution.service.event.SolutionAddedEvent;
import com.devblocker.solution.service.event.SolutionUpvotedEvent;
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
    
    public void publishSolutionAdded(Solution solution) {
        try {
            SolutionAddedEvent event = SolutionAddedEvent.builder()
                    .solutionId(solution.getSolutionId().toString())
                    .blockerId(solution.getBlockerId().toString())
                    .userId(solution.getUserId().toString())
                    .content(solution.getContent())
                    .createdAt(solution.getCreatedAt())
                    .build();
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.SOLUTION_ADDED_ROUTING_KEY,
                    event
            );
            log.info("Published SolutionAdded event for solution: {}", solution.getSolutionId());
        } catch (Exception e) {
            log.error("Failed to publish SolutionAdded event for solution: {}", 
                    solution.getSolutionId(), e);
        }
    }
    
    public void publishSolutionUpvoted(Solution solution, UUID userId) {
        try {
            SolutionUpvotedEvent event = SolutionUpvotedEvent.builder()
                    .solutionId(solution.getSolutionId().toString())
                    .blockerId(solution.getBlockerId().toString())
                    .userId(userId.toString())
                    .upvotes(solution.getUpvotes())
                    .upvotedAt(java.time.LocalDateTime.now())
                    .build();
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.SOLUTION_UPVOTED_ROUTING_KEY,
                    event
            );
            log.info("Published SolutionUpvoted event for solution: {}", solution.getSolutionId());
        } catch (Exception e) {
            log.error("Failed to publish SolutionUpvoted event for solution: {}", 
                    solution.getSolutionId(), e);
        }
    }
    
    public void publishSolutionAccepted(Solution solution, UUID acceptedBy) {
        try {
            SolutionAcceptedEvent event = SolutionAcceptedEvent.builder()
                    .solutionId(solution.getSolutionId().toString())
                    .blockerId(solution.getBlockerId().toString())
                    .userId(solution.getUserId().toString())
                    .acceptedBy(acceptedBy.toString())
                    .acceptedAt(java.time.LocalDateTime.now())
                    .build();
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.SOLUTION_ACCEPTED_ROUTING_KEY,
                    event
            );
            log.info("Published SolutionAccepted event for solution: {}", solution.getSolutionId());
        } catch (Exception e) {
            log.error("Failed to publish SolutionAccepted event for solution: {}", 
                    solution.getSolutionId(), e);
        }
    }
}

