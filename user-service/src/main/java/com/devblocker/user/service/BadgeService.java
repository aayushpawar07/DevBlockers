package com.devblocker.user.service;

import com.devblocker.user.dto.BadgeResponse;
import com.devblocker.user.dto.CreateBadgeRequest;
import com.devblocker.user.dto.PageResponse;
import com.devblocker.user.dto.UserBadgeResponse;
import com.devblocker.user.model.Badge;
import com.devblocker.user.model.UserBadge;
import com.devblocker.user.repository.BadgeRepository;
import com.devblocker.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {
    
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    
    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findAll().stream()
                .map(this::mapToBadgeResponse)
                .collect(Collectors.toList());
    }
    
    public BadgeResponse getBadge(UUID badgeId) {
        Badge badge = badgeRepository.findByBadgeId(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found: " + badgeId));
        return mapToBadgeResponse(badge);
    }
    
    @Transactional
    public BadgeResponse createBadge(CreateBadgeRequest request) {
        if (badgeRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Badge with name already exists: " + request.getName());
        }
        
        Badge badge = Badge.builder()
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .reputationThreshold(request.getReputationThreshold())
                .build();
        
        badge = badgeRepository.save(badge);
        log.info("Badge created: {}", badge.getName());
        
        return mapToBadgeResponse(badge);
    }
    
    public List<UserBadgeResponse> getUserBadges(UUID userId) {
        return userBadgeRepository.findByUserId(userId).stream()
                .map(this::mapToUserBadgeResponse)
                .collect(Collectors.toList());
    }
    
    public PageResponse<UserBadgeResponse> getUserBadges(UUID userId, Pageable pageable) {
        Page<UserBadge> userBadges = userBadgeRepository.findByUserId(userId, pageable);
        
        return PageResponse.<UserBadgeResponse>builder()
                .content(userBadges.getContent().stream()
                        .map(this::mapToUserBadgeResponse)
                        .collect(Collectors.toList()))
                .page(userBadges.getNumber())
                .size(userBadges.getSize())
                .totalElements(userBadges.getTotalElements())
                .totalPages(userBadges.getTotalPages())
                .first(userBadges.isFirst())
                .last(userBadges.isLast())
                .build();
    }
    
    @Transactional
    public UserBadgeResponse awardBadge(UUID userId, UUID badgeId) {
        // Check if badge exists
        Badge badge = badgeRepository.findByBadgeId(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found: " + badgeId));
        
        // Check if user already has this badge
        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId)) {
            throw new IllegalArgumentException("User already has this badge");
        }
        
        UserBadge userBadge = UserBadge.builder()
                .userId(userId)
                .badgeId(badgeId)
                .build();
        
        userBadge = userBadgeRepository.save(userBadge);
        log.info("Badge {} awarded to user: {}", badge.getName(), userId);
        
        return mapToUserBadgeResponse(userBadge);
    }
    
    @Transactional
    public void checkAndAwardReputationBadges(UUID userId, Integer newReputation) {
        List<Badge> badges = badgeRepository.findAll();
        
        for (Badge badge : badges) {
            if (badge.getReputationThreshold() != null && 
                newReputation >= badge.getReputationThreshold() &&
                !userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getBadgeId())) {
                
                awardBadge(userId, badge.getBadgeId());
            }
        }
    }
    
    private BadgeResponse mapToBadgeResponse(Badge badge) {
        return BadgeResponse.builder()
                .badgeId(badge.getBadgeId())
                .name(badge.getName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .reputationThreshold(badge.getReputationThreshold())
                .createdAt(badge.getCreatedAt())
                .build();
    }
    
    private UserBadgeResponse mapToUserBadgeResponse(UserBadge userBadge) {
        Badge badge = userBadge.getBadge();
        return UserBadgeResponse.builder()
                .userBadgeId(userBadge.getUserBadgeId())
                .badgeId(userBadge.getBadgeId())
                .badgeName(badge != null ? badge.getName() : null)
                .badgeDescription(badge != null ? badge.getDescription() : null)
                .iconUrl(badge != null ? badge.getIconUrl() : null)
                .earnedAt(userBadge.getEarnedAt())
                .build();
    }
}

