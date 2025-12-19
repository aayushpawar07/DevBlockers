package com.devblocker.blocker.service;

import com.devblocker.blocker.client.UserServiceClient;
import com.devblocker.blocker.dto.BlockerResponse;
import com.devblocker.blocker.dto.CreateBlockerRequest;
import com.devblocker.blocker.dto.PageResponse;
import com.devblocker.blocker.dto.ResolveBlockerRequest;
import com.devblocker.blocker.dto.UpdateBestSolutionRequest;
import com.devblocker.blocker.dto.UpdateBlockerRequest;
import com.devblocker.blocker.model.Blocker;
import com.devblocker.blocker.model.BlockerStatus;
import com.devblocker.blocker.model.BlockerVisibility;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockerService {
    
    private final BlockerRepository blockerRepository;
    private final EventPublisher eventPublisher;
    private final DuplicateDetectionService duplicateDetectionService;
    private final UserServiceClient userServiceClient;
    
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
                .visibility(request.getVisibility() != null ? request.getVisibility() : BlockerVisibility.PUBLIC)
                .orgId(request.getOrgId())
                .groupId(request.getGroupId())
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
    
    public BlockerResponse getBlocker(UUID blockerId, UUID userOrgId, java.util.List<UUID> userGroupIds) {
        Blocker blocker = blockerRepository.findByBlockerId(blockerId)
                .orElseThrow(() -> new IllegalArgumentException("Blocker not found: " + blockerId));
        
        // Check access
        if (!canAccessBlocker(blocker, userOrgId, userGroupIds)) {
            throw new IllegalArgumentException("Unauthorized: You don't have access to this blocker");
        }
        
        return mapToBlockerResponse(blocker);
    }
    
    private boolean canAccessBlocker(Blocker blocker, UUID userOrgId, java.util.List<UUID> userGroupIds) {
        if (blocker.getVisibility() == com.devblocker.blocker.model.BlockerVisibility.PUBLIC) {
            return true;
        }
        if (blocker.getVisibility() == com.devblocker.blocker.model.BlockerVisibility.ORG) {
            return userOrgId != null && userOrgId.equals(blocker.getOrgId());
        }
        if (blocker.getVisibility() == com.devblocker.blocker.model.BlockerVisibility.GROUP) {
            return userGroupIds != null && blocker.getGroupId() != null && userGroupIds.contains(blocker.getGroupId());
        }
        return false;
    }
    
    public PageResponse<BlockerResponse> getAllBlockers(
            BlockerStatus status,
            Severity severity,
            UUID createdBy,
            UUID assignedTo,
            UUID teamId,
            String teamCode,
            String tag,
            UUID userOrgId,
            java.util.List<UUID> userGroupIds,
            int page,
            int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Blocker> blockers = blockerRepository.findWithFilters(
                status, severity, createdBy, assignedTo, teamId, tag, userOrgId, userGroupIds, pageable);
        
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
    
    private List<String> getUserTeamCodes(UUID userId) {
        return userServiceClient.getUserTeamCodes(userId, null);
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
        
        // Authorization: Only team members can resolve blockers
        if (resolvedBy != null && blocker.getTeamCode() != null) {
            List<String> userTeamCodes = getUserTeamCodes(resolvedBy);
            if (!userTeamCodes.contains(blocker.getTeamCode())) {
                throw new IllegalStateException("Only team members can resolve blockers for this team");
            }
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
                .teamCode(blocker.getTeamCode())
                .bestSolutionId(blocker.getBestSolutionId())
                .tags(blocker.getTags())
                .mediaUrls(blocker.getMediaUrls())
                .createdAt(blocker.getCreatedAt())
                .updatedAt(blocker.getUpdatedAt())
                .resolvedAt(blocker.getResolvedAt())
                .build();
    }
}

