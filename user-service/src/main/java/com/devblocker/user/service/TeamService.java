package com.devblocker.user.service;

import com.devblocker.user.dto.CreateTeamRequest;
import com.devblocker.user.dto.PageResponse;
import com.devblocker.user.dto.TeamMemberResponse;
import com.devblocker.user.dto.TeamResponse;
import com.devblocker.user.dto.UpdateTeamRequest;
import com.devblocker.user.model.Profile;
import com.devblocker.user.model.Team;
import com.devblocker.user.repository.ProfileRepository;
import com.devblocker.user.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {
    
    private final TeamRepository teamRepository;
    private final ProfileRepository profileRepository;
    
    public TeamMemberResponse getTeamMembers(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        
        List<Profile> profiles = profileRepository.findByTeamId(teamId);
        
        List<TeamMemberResponse.MemberInfo> members = profiles.stream()
                .map(profile -> TeamMemberResponse.MemberInfo.builder()
                        .userId(profile.getUserId())
                        .name(profile.getName())
                        .avatarUrl(profile.getAvatarUrl())
                        .joinedAt(profile.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return TeamMemberResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getName())
                .members(members)
                .build();
    }
    
    public TeamResponse getTeam(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        
        int memberCount = profileRepository.findByTeamId(teamId).size();
        
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .name(team.getName())
                .memberCount(memberCount)
                .createdAt(team.getCreatedAt())
                .build();
    }
    
    public PageResponse<TeamResponse> getAllTeams(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Team> teams = teamRepository.findAll(pageable);
        
        return PageResponse.<TeamResponse>builder()
                .content(teams.getContent().stream()
                        .map(team -> {
                            int memberCount = profileRepository.findByTeamId(team.getTeamId()).size();
                            return TeamResponse.builder()
                                    .teamId(team.getTeamId())
                                    .name(team.getName())
                                    .memberCount(memberCount)
                                    .createdAt(team.getCreatedAt())
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .page(teams.getNumber())
                .size(teams.getSize())
                .totalElements(teams.getTotalElements())
                .totalPages(teams.getTotalPages())
                .first(teams.isFirst())
                .last(teams.isLast())
                .build();
    }
    
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request) {
        Team team = Team.builder()
                .name(request.getName())
                .build();
        
        team = teamRepository.save(team);
        log.info("Team created: {}", team.getName());
        
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .name(team.getName())
                .memberCount(0)
                .createdAt(team.getCreatedAt())
                .build();
    }
    
    @Transactional
    public TeamResponse updateTeam(UUID teamId, UpdateTeamRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        
        team.setName(request.getName());
        team = teamRepository.save(team);
        
        log.info("Team updated: {}", team.getName());
        
        int memberCount = profileRepository.findByTeamId(teamId).size();
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .name(team.getName())
                .memberCount(memberCount)
                .createdAt(team.getCreatedAt())
                .build();
    }
    
    @Transactional
    public void deleteTeam(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        
        // Remove team from all profiles
        List<Profile> profiles = profileRepository.findByTeamId(teamId);
        for (Profile profile : profiles) {
            profile.setTeamId(null);
            profileRepository.save(profile);
        }
        
        teamRepository.delete(team);
        log.info("Team deleted: {}", teamId);
    }
    
    @Transactional
    public void addMemberToTeam(UUID teamId, UUID userId) {
        // Validate team exists
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("Team not found: " + teamId);
        }
        
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));
        
        profile.setTeamId(teamId);
        profileRepository.save(profile);
        
        log.info("User {} added to team {}", userId, teamId);
    }
    
    @Transactional
    public void removeMemberFromTeam(UUID teamId, UUID userId) {
        // Validate team exists
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("Team not found: " + teamId);
        }
        
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));
        
        if (!teamId.equals(profile.getTeamId())) {
            throw new IllegalArgumentException("User is not a member of this team");
        }
        
        profile.setTeamId(null);
        profileRepository.save(profile);
        
        log.info("User {} removed from team {}", userId, teamId);
    }
}

