package com.devblocker.solution.service;

import com.devblocker.solution.client.BlockerServiceClient;
import com.devblocker.solution.dto.AcceptSolutionRequest;
import com.devblocker.solution.dto.CreateSolutionRequest;
import com.devblocker.solution.dto.SolutionResponse;
import com.devblocker.solution.dto.UpvoteRequest;
import com.devblocker.solution.model.Solution;
import com.devblocker.solution.model.SolutionUpvote;
import com.devblocker.solution.repository.SolutionRepository;
import com.devblocker.solution.repository.SolutionUpvoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolutionService {
    
    private final SolutionRepository solutionRepository;
    private final SolutionUpvoteRepository upvoteRepository;
    private final EventPublisher eventPublisher;
    private final BlockerServiceClient blockerServiceClient;
    
    @Transactional
    public SolutionResponse addSolution(UUID blockerId, CreateSolutionRequest request, String authToken) {
        // Validate blocker exists
        if (!blockerServiceClient.blockerExists(blockerId, authToken)) {
            throw new IllegalArgumentException("Blocker not found: " + blockerId);
        }
        
        // Check if user already has a solution for this blocker (optional business rule)
        // For now, allow multiple solutions per user
        
        Solution solution = Solution.builder()
                .blockerId(blockerId)
                .userId(request.getUserId())
                .content(request.getContent())
                .mediaUrls(request.getMediaUrls() != null ? request.getMediaUrls() : new java.util.ArrayList<>())
                .upvotes(0)
                .accepted(false)
                .build();
        
        final Solution savedSolution = solutionRepository.save(solution);
        
        // Publish event after transaction commit
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishSolutionAdded(savedSolution);
                    }
                }
        );
        
        log.info("Solution created: {} for blocker: {}", solution.getSolutionId(), blockerId);
        
        return mapToResponse(solution);
    }
    
    public List<SolutionResponse> getSolutionsByBlocker(UUID blockerId) {
        List<Solution> solutions = solutionRepository.findByBlockerIdOrderByUpvotesDescCreatedAtAsc(blockerId);
        return solutions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public SolutionResponse upvoteSolution(UUID solutionId, UpvoteRequest request) {
        Solution solution = solutionRepository.findBySolutionId(solutionId)
                .orElseThrow(() -> new IllegalArgumentException("Solution not found: " + solutionId));
        
        // Check if user already upvoted (idempotent)
        if (upvoteRepository.existsBySolutionIdAndUserId(solutionId, request.getUserId())) {
            log.debug("User {} already upvoted solution {}", request.getUserId(), solutionId);
            return mapToResponse(solution); // Return current state without error
        }
        
        // Create upvote record
        SolutionUpvote upvote = SolutionUpvote.builder()
                .solutionId(solutionId)
                .userId(request.getUserId())
                .build();
        upvoteRepository.save(upvote);
        
        // Increment upvote count
        solution.setUpvotes(solution.getUpvotes() + 1);
        final Solution savedSolution = solutionRepository.save(solution);
        final UUID finalUserId = request.getUserId();
        
        // Publish event after transaction commit
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishSolutionUpvoted(savedSolution, finalUserId);
                    }
                }
        );
        
        log.info("Solution {} upvoted by user {}, total upvotes: {}", 
                solutionId, finalUserId, savedSolution.getUpvotes());
        
        return mapToResponse(solution);
    }
    
    @Transactional
    public SolutionResponse acceptSolution(UUID solutionId, AcceptSolutionRequest request, String authToken) {
        Solution solution = solutionRepository.findBySolutionId(solutionId)
                .orElseThrow(() -> new IllegalArgumentException("Solution not found: " + solutionId));
        
        // Check if solution is already accepted
        if (solution.getAccepted()) {
            log.warn("Solution {} is already accepted", solutionId);
            return mapToResponse(solution);
        }
        
        // Check if blocker already has an accepted solution
        if (solutionRepository.existsByBlockerIdAndAcceptedTrue(solution.getBlockerId())) {
            throw new IllegalStateException("Blocker already has an accepted solution");
        }
        
        // Mark solution as accepted
        solution.setAccepted(true);
        final Solution savedSolution = solutionRepository.save(solution);
        final UUID finalAcceptedBy = request.getUserId();
        
        // Update blocker's best solution via REST call
        boolean updated = blockerServiceClient.updateBestSolution(
                savedSolution.getBlockerId(), 
                solutionId, 
                authToken
        );
        
        if (!updated) {
            log.warn("Failed to update blocker's best solution, but solution is marked as accepted");
        }
        
        // Publish event after transaction commit
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishSolutionAccepted(savedSolution, finalAcceptedBy);
                    }
                }
        );
        
        log.info("Solution {} accepted for blocker: {}", solutionId, savedSolution.getBlockerId());
        
        return mapToResponse(savedSolution);
    }
    
    public SolutionResponse getSolution(UUID solutionId) {
        Solution solution = solutionRepository.findBySolutionId(solutionId)
                .orElseThrow(() -> new IllegalArgumentException("Solution not found: " + solutionId));
        return mapToResponse(solution);
    }
    
    public long getSolutionCountByUser(UUID userId) {
        return solutionRepository.countByUserId(userId);
    }
    
    public long getAcceptedSolutionCountByUser(UUID userId) {
        return solutionRepository.countByUserIdAndAcceptedTrue(userId);
    }
    
    private SolutionResponse mapToResponse(Solution solution) {
        return SolutionResponse.builder()
                .solutionId(solution.getSolutionId())
                .blockerId(solution.getBlockerId())
                .userId(solution.getUserId())
                .content(solution.getContent())
                .mediaUrls(solution.getMediaUrls())
                .upvotes(solution.getUpvotes())
                .accepted(solution.getAccepted())
                .createdAt(solution.getCreatedAt())
                .build();
    }
}

