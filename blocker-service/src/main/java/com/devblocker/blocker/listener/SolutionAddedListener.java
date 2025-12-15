package com.devblocker.blocker.listener;

import com.devblocker.blocker.model.Blocker;
import com.devblocker.blocker.repository.BlockerRepository;
import com.devblocker.blocker.service.event.SolutionAddedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolutionAddedListener {
    
    private final BlockerRepository blockerRepository;
    
    @RabbitListener(queues = "solution.added.queue")
    @Transactional
    public void handleSolutionAdded(SolutionAddedEvent event) {
        try {
            UUID blockerId = UUID.fromString(event.getBlockerId());
            UUID solutionId = UUID.fromString(event.getSolutionId());
            
            log.info("Received SolutionAdded event for blocker: {}, solution: {}", blockerId, solutionId);
            
            Blocker blocker = blockerRepository.findByBlockerId(blockerId)
                    .orElseThrow(() -> new IllegalArgumentException("Blocker not found: " + blockerId));
            
            // If no best solution is set yet, set this as the best solution
            // Or implement logic to determine best solution based on votes/quality
            if (blocker.getBestSolutionId() == null) {
                blocker.setBestSolutionId(solutionId);
                blockerRepository.save(blocker);
                log.info("Set solution {} as best solution for blocker: {}", solutionId, blockerId);
            } else {
                log.debug("Blocker {} already has a best solution: {}", blockerId, blocker.getBestSolutionId());
                // TODO: Implement logic to compare and update best solution if needed
            }
            
        } catch (Exception e) {
            log.error("Failed to process SolutionAdded event: {}", event, e);
            // Consider implementing retry logic or dead letter queue
        }
    }
}

