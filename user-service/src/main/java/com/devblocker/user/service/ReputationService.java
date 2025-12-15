package com.devblocker.user.service;

import com.devblocker.user.dto.PageResponse;
import com.devblocker.user.dto.ReputationResponse;
import com.devblocker.user.dto.ReputationTransactionResponse;
import com.devblocker.user.model.Reputation;
import com.devblocker.user.model.ReputationTransaction;
import com.devblocker.user.repository.ReputationRepository;
import com.devblocker.user.repository.ReputationTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReputationService {
    
    private final ReputationRepository reputationRepository;
    private final ReputationTransactionRepository transactionRepository;
    private final BadgeService badgeService;
    
    public ReputationResponse getReputation(UUID userId) {
        Reputation reputation = reputationRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Reputation not found for user: " + userId));
        
        return ReputationResponse.builder()
                .userId(reputation.getUserId())
                .points(reputation.getPoints())
                .createdAt(reputation.getCreatedAt())
                .updatedAt(reputation.getUpdatedAt())
                .build();
    }
    
    @Transactional
    public ReputationResponse incrementReputation(UUID userId, Integer points, String reason, String source) {
        Reputation reputation = reputationRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Reputation not found for user: " + userId));
        
        reputationRepository.incrementPoints(userId, points);
        
        // Create transaction record
        ReputationTransaction transaction = ReputationTransaction.builder()
                .userId(userId)
                .points(points)
                .reason(reason != null ? reason : "Reputation incremented")
                .source(source != null ? source : "API")
                .build();
        transactionRepository.save(transaction);
        
        // Refresh to get updated values
        reputation = reputationRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Reputation not found for user: " + userId));
        
        // Check and award badges based on new reputation
        badgeService.checkAndAwardReputationBadges(userId, reputation.getPoints());
        
        log.info("Reputation incremented by {} points for user: {}. New total: {}", 
                points, userId, reputation.getPoints());
        
        return ReputationResponse.builder()
                .userId(reputation.getUserId())
                .points(reputation.getPoints())
                .createdAt(reputation.getCreatedAt())
                .updatedAt(reputation.getUpdatedAt())
                .build();
    }
    
    @Transactional
    public ReputationResponse incrementReputation(UUID userId, Integer points) {
        return incrementReputation(userId, points, null, null);
    }
    
    public PageResponse<ReputationTransactionResponse> getReputationHistory(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReputationTransaction> transactions = transactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return PageResponse.<ReputationTransactionResponse>builder()
                .content(transactions.getContent().stream()
                        .map(this::mapToTransactionResponse)
                        .collect(Collectors.toList()))
                .page(transactions.getNumber())
                .size(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .first(transactions.isFirst())
                .last(transactions.isLast())
                .build();
    }
    
    public PageResponse<ReputationTransactionResponse> getReputationHistoryByDateRange(
            UUID userId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReputationTransaction> transactions = transactionRepository
                .findByUserIdAndDateRange(userId, startDate, endDate, pageable);
        
        return PageResponse.<ReputationTransactionResponse>builder()
                .content(transactions.getContent().stream()
                        .map(this::mapToTransactionResponse)
                        .collect(Collectors.toList()))
                .page(transactions.getNumber())
                .size(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .first(transactions.isFirst())
                .last(transactions.isLast())
                .build();
    }
    
    private ReputationTransactionResponse mapToTransactionResponse(ReputationTransaction transaction) {
        return ReputationTransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUserId())
                .points(transaction.getPoints())
                .reason(transaction.getReason())
                .source(transaction.getSource())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

