package com.devblocker.user.service;

import com.devblocker.user.dto.ProfileResponse;
import com.devblocker.user.dto.UpdateProfileRequest;
import com.devblocker.user.model.Profile;
import com.devblocker.user.model.Reputation;
import com.devblocker.user.repository.ProfileRepository;
import com.devblocker.user.repository.ReputationRepository;
import com.devblocker.user.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    private final ReputationRepository reputationRepository;
    private final TeamRepository teamRepository;
    private final EventPublisher eventPublisher;
    private final com.devblocker.user.client.BlockerServiceClient blockerServiceClient;
    private final com.devblocker.user.client.SolutionServiceClient solutionServiceClient;
    
    @Transactional
    public ProfileResponse createProfile(UUID userId, String email) {
        Profile profile = Profile.builder()
                .userId(userId)
                .name(email.split("@")[0]) // Default name from email
                .avatarUrl(null)
                .teamId(null)
                .build();
        
        profile = profileRepository.save(profile);
        
        // Create initial reputation
        Reputation reputation = Reputation.builder()
                .userId(userId)
                .points(0)
                .build();
        reputationRepository.save(reputation);
        
        log.info("Profile created for user: {}", userId);
        
        return mapToProfileResponse(profile);
    }
    
    public ProfileResponse getProfile(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Create profile if it doesn't exist
                    log.info("Profile not found for user: {}, creating default profile", userId);
                    Profile newProfile = Profile.builder()
                            .userId(userId)
                            .name("User") // Default name
                            .avatarUrl(null)
                            .teamId(null)
                            .build();
                    newProfile = profileRepository.save(newProfile);
                    
                    // Create initial reputation if it doesn't exist
                    if (!reputationRepository.findByUserId(userId).isPresent()) {
                        Reputation reputation = Reputation.builder()
                                .userId(userId)
                                .points(0)
                                .build();
                        reputationRepository.save(reputation);
                    }
                    
                    return newProfile;
                });
        
        return mapToProfileResponse(profile);
    }
    
    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        // Get or create profile
        Profile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Create profile if it doesn't exist
                    log.info("Profile not found for user: {}, creating profile from update request", userId);
                    Profile newProfile = Profile.builder()
                            .userId(userId)
                            .name(request.getName()) // Use name from request
                            .avatarUrl(request.getAvatarUrl())
                            .bio(request.getBio())
                            .location(request.getLocation())
                            .teamId(request.getTeamId())
                            .build();
                    newProfile = profileRepository.save(newProfile);
                    
                    // Create initial reputation if it doesn't exist
                    if (!reputationRepository.findByUserId(userId).isPresent()) {
                        Reputation reputation = Reputation.builder()
                                .userId(userId)
                                .points(0)
                                .build();
                        reputationRepository.save(reputation);
                        log.info("Created initial reputation for user: {}", userId);
                    }
                    
                    return newProfile;
                });
        
        // Validate team if provided
        if (request.getTeamId() != null) {
            teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Team not found: " + request.getTeamId()));
        }
        
        // Update profile
        profile.setName(request.getName());
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        profile.setBio(request.getBio());
        profile.setLocation(request.getLocation());
        profile.setTeamId(request.getTeamId());
        
        profile = profileRepository.save(profile);
        
        // Publish UserUpdated event
        eventPublisher.publishUserUpdated(userId, profile);
        
        log.info("Profile updated for user: {}", userId);
        
        return mapToProfileResponse(profile);
    }
    
    public com.devblocker.user.dto.PageResponse<ProfileResponse> searchProfiles(
            String name, UUID teamId, Integer minReputation, Integer maxReputation, int page, int size) {
        org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size);
        
        org.springframework.data.domain.Page<Profile> profiles;
        
        if (minReputation != null || maxReputation != null) {
            profiles = profileRepository.searchProfilesWithReputation(
                    name, teamId, minReputation, maxReputation, pageable);
        } else {
            profiles = profileRepository.searchProfiles(name, teamId, pageable);
        }
        
        return com.devblocker.user.dto.PageResponse.<ProfileResponse>builder()
                .content(profiles.getContent().stream()
                        .map(this::mapToProfileResponse)
                        .collect(java.util.stream.Collectors.toList()))
                .page(profiles.getNumber())
                .size(profiles.getSize())
                .totalElements(profiles.getTotalElements())
                .totalPages(profiles.getTotalPages())
                .first(profiles.isFirst())
                .last(profiles.isLast())
                .build();
    }
    
    private ProfileResponse mapToProfileResponse(Profile profile) {
        String teamName = null;
        if (profile.getTeamId() != null && profile.getTeam() != null) {
            teamName = profile.getTeam().getName();
        }
        
        // Fetch stats from other services
        int solutionsCount = 0;
        int acceptedSolutionsCount = 0;
        int blockersCount = 0;
        
        try {
            // Get solution stats
            com.devblocker.user.client.SolutionServiceClient.UserSolutionStats solutionStats = 
                    solutionServiceClient.getSolutionStats(profile.getUserId(), null);
            solutionsCount = solutionStats.getTotalSolutions();
            acceptedSolutionsCount = solutionStats.getAcceptedSolutions();
            
            // Get blocker stats
            com.devblocker.user.client.BlockerServiceClient.UserBlockerStats blockerStats = 
                    blockerServiceClient.getBlockerStats(profile.getUserId(), null);
            blockersCount = blockerStats.getTotalBlockers();
        } catch (Exception e) {
            log.warn("Failed to fetch stats for user: {}", profile.getUserId(), e);
            // Stats will remain 0 on error
        }
        
        return ProfileResponse.builder()
                .userId(profile.getUserId())
                .name(profile.getName())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .location(profile.getLocation())
                .teamId(profile.getTeamId())
                .teamName(teamName)
                .solutionsCount(solutionsCount)
                .acceptedSolutionsCount(acceptedSolutionsCount)
                .blockersCount(blockersCount)
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}

