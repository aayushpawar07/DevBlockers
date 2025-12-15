package com.devblocker.blocker.listener;

import com.devblocker.blocker.client.UserServiceClient;
import com.devblocker.blocker.model.Blocker;
import com.devblocker.blocker.model.BlockerStatus;
import com.devblocker.blocker.repository.BlockerRepository;
import com.devblocker.blocker.service.event.SolutionAcceptedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolutionAcceptedListener {
    
    private final BlockerRepository blockerRepository;
    private final UserServiceClient userServiceClient;
    
    @Value("${reputation.points.solution.accepted:50}")
    private Integer solutionAcceptedPoints;
    
    @RabbitListener(queues = "solution.accepted.queue")
    @Transactional
    public void handleSolutionAccepted(SolutionAcceptedEvent event) {
        try {
            UUID blockerId = UUID.fromString(event.getBlockerId());
            UUID solutionId = UUID.fromString(event.getSolutionId());
            UUID solutionProviderId = UUID.fromString(event.getUserId());
            
            log.info("Received SolutionAccepted event for blocker: {}, solution: {}, provider: {}", 
                    blockerId, solutionId, solutionProviderId);
            
            Blocker blocker = blockerRepository.findByBlockerId(blockerId)
                    .orElseThrow(() -> new IllegalArgumentException("Blocker not found: " + blockerId));
            
            // Close the blocker if it's not already closed
            if (blocker.getStatus() != BlockerStatus.CLOSED) {
                blocker.setStatus(BlockerStatus.CLOSED);
                blocker.setBestSolutionId(solutionId);
                blocker.setResolvedAt(LocalDateTime.now());
                blockerRepository.save(blocker);
                log.info("Closed blocker: {} with accepted solution: {}", blockerId, solutionId);
            } else {
                log.debug("Blocker {} is already closed", blockerId);
            }
            
            // Award points to the solution provider
            boolean pointsAwarded = userServiceClient.incrementReputation(
                    solutionProviderId,
                    solutionAcceptedPoints,
                    String.format("Solution accepted for blocker: %s", blocker.getTitle()),
                    "SOLUTION_ACCEPTED",
                    null // No auth token needed for internal service calls
            );
            
            if (pointsAwarded) {
                log.info("Awarded {} points to user {} for accepted solution", 
                        solutionAcceptedPoints, solutionProviderId);
            } else {
                log.warn("Failed to award points to user {} for accepted solution", solutionProviderId);
            }
            
        } catch (Exception e) {
            log.error("Failed to process SolutionAccepted event: {}", event, e);
            // Consider implementing retry logic or dead letter queue
        }
    }
}

