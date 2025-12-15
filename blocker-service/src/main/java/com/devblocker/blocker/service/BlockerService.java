package com.devblocker.blocker.service;

import com.devblocker.blocker.dto.BlockerResponse;
import com.devblocker.blocker.dto.CreateBlockerRequest;
import com.devblocker.blocker.dto.PageResponse;
import com.devblocker.blocker.dto.ResolveBlockerRequest;
import com.devblocker.blocker.dto.UpdateBestSolutionRequest;
import com.devblocker.blocker.dto.UpdateBlockerRequest;
import com.devblocker.blocker.model.Blocker;
import com.devblocker.blocker.model.BlockerStatus;
import com.devblocker.blocker.model.Severity;
import com.devblocker.blocker.repository.BlockerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockerService {
    
    private final BlockerRepository blockerRepository;
    private final EventPublisher eventPublisher;
    private final DuplicateDetectionService duplicateDetectionService;
    
    @Transactional
    public BlockerResponse createBlocker(CreateBlockerRequest request) {
        Blocker blocker = Blocker.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(BlockerStatus.OPEN)
                .createdBy(request.getCreatedBy())
                .assignedTo(request.getAssignedTo())
                .teamId(request.getTeamId())
                .tags(request.getTags() != null ? request.getTags() : new java.util.ArrayList<>())
                .mediaUrls(request.getMediaUrls() != null ? request.getMediaUrls() : new java.util.ArrayList<>())
                .build();
        
        final Blocker savedBlocker = blockerRepository.save(blocker);
        
        // Publish event after transaction commit using TransactionSynchronization
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishBlockerCreated(savedBlocker);
                        // Trigger duplicate detection after commit
                        duplicateDetectionService.checkDuplicates(savedBlocker);
                    }
                }
        );
        
        log.info("Blocker created: {}", savedBlocker.getBlockerId());
        
        return mapToBlockerResponse(savedBlocker);
    }
    
    public BlockerResponse getBlocker(UUID blockerId) {
        Blocker blocker = blockerRepository.findByBlockerId(blockerId)
                .orElseThrow(() -> new IllegalArgumentException("Blocker not found: " + blockerId));
        
        return mapToBlockerResponse(blocker);
    }
    
    public PageResponse<BlockerResponse> getAllBlockers(
            BlockerStatus status,
            Severity severity,
            UUID createdBy,
            UUID assignedTo,
            UUID teamId,
            String tag,
            int page,
            int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Blocker> blockers = blockerRepository.findWithFilters(
                status, severity, createdBy, assignedTo, teamId, tag, pageable);
        
        return PageResponse.<BlockerResponse>builder()
                .content(blockers.getContent().stream()
                        .map(this::mapToBlockerResponse)
                        .collect(Collectors.toList()))
                .page(blockers.getNumber())
                .size(blockers.getSize())
                .totalElements(blockers.getTotalElements())
                .totalPages(blockers.getTotalPages())
                .first(blockers.isFirst())
                .last(blockers.isLast())
                .build();
    }
    
    @Transactional
    public BlockerResponse updateBlocker(UUID blockerId, UpdateBlockerRequest request) {
        Blocker blocker = blockerRepository.findByBlockerId(blockerId)
                .orElseThrow(() -> new IllegalArgumentException("Blocker not found: " + blockerId));
        
        if (request.getTitle() != null) {
            blocker.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            blocker.setDescription(request.getDescription());
        }
        if (request.getSeverity() != null) {
            blocker.setSeverity(request.getSeverity());
        }
        if (request.getAssignedTo() != null) {
            blocker.setAssignedTo(request.getAssignedTo());
        }
        if (request.getTeamId() != null) {
            blocker.setTeamId(request.getTeamId());
        }
        if (request.getTags() != null) {
            blocker.setTags(request.getTags());
        }
        if (request.getMediaUrls() != null) {
            blocker.setMediaUrls(request.getMediaUrls());
        }
        
        final Blocker savedBlocker = blockerRepository.save(blocker);
        
        // Publish event after transaction commit
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishBlockerUpdated(savedBlocker);
                    }
                }
        );
        
        log.info("Blocker updated: {}", blockerId);
        
        return mapToBlockerResponse(savedBlocker);
    }
    
    @Transactional
    public BlockerResponse resolveBlocker(UUID blockerId, ResolveBlockerRequest request, UUID resolvedBy) {
        Blocker blocker = blockerRepository.findByBlockerId(blockerId)
                .orElseThrow(() -> new IllegalArgumentException("Blocker not found: " + blockerId));
        
        if (blocker.getStatus() == BlockerStatus.RESOLVED) {
            throw new IllegalArgumentException("Blocker is already resolved");
        }
        
        blocker.setStatus(BlockerStatus.RESOLVED);
        blocker.setBestSolutionId(request.getBestSolutionId());
        blocker.setResolvedAt(LocalDateTime.now());
        
        final Blocker savedBlocker = blockerRepository.save(blocker);
        final UUID finalResolvedBy = resolvedBy;
        
        // Publish event after transaction commit
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishBlockerResolved(savedBlocker, finalResolvedBy);
                    }
                }
        );
        
        log.info("Blocker resolved: {}", blockerId);
        
        return mapToBlockerResponse(savedBlocker);
    }
    
    @Transactional
    public BlockerResponse updateBestSolution(UUID blockerId, UpdateBestSolutionRequest request) {
        Blocker blocker = blockerRepository.findByBlockerId(blockerId)
                .orElseThrow(() -> new IllegalArgumentException("Blocker not found: " + blockerId));
        
        blocker.setBestSolutionId(request.getBestSolutionId());
        final Blocker savedBlocker = blockerRepository.save(blocker);
        
        // Publish event after transaction commit
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishBlockerUpdated(savedBlocker);
                    }
                }
        );
        
        log.info("Best solution updated for blocker: {} to solution: {}", blockerId, request.getBestSolutionId());
        
        return mapToBlockerResponse(savedBlocker);
    }
    
    private BlockerResponse mapToBlockerResponse(Blocker blocker) {
        return BlockerResponse.builder()
                .blockerId(blocker.getBlockerId())
                .title(blocker.getTitle())
                .description(blocker.getDescription())
                .status(blocker.getStatus())
                .severity(blocker.getSeverity())
                .createdBy(blocker.getCreatedBy())
                .assignedTo(blocker.getAssignedTo())
                .teamId(blocker.getTeamId())
                .bestSolutionId(blocker.getBestSolutionId())
                .tags(blocker.getTags())
                .mediaUrls(blocker.getMediaUrls())
                .createdAt(blocker.getCreatedAt())
                .updatedAt(blocker.getUpdatedAt())
                .resolvedAt(blocker.getResolvedAt())
                .build();
    }
}

