package com.devblocker.user.service;

import com.devblocker.user.dto.CreateTeamRequest;
import com.devblocker.user.dto.PageResponse;
import com.devblocker.user.dto.TeamMemberResponse;
import com.devblocker.user.dto.TeamResponse;
import com.devblocker.user.dto.UpdateTeamRequest;
import com.devblocker.user.model.Profile;
import com.devblocker.user.model.Team;
import com.devblocker.user.model.TeamCode;
import com.devblocker.user.model.UserTeam;
import com.devblocker.user.repository.ProfileRepository;
import com.devblocker.user.repository.TeamRepository;
import com.devblocker.user.repository.UserTeamRepository;
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
    private final UserTeamRepository userTeamRepository;
    
    public TeamMemberResponse getTeamMembers(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        
        List<UserTeam> userTeams = userTeamRepository.findByTeamId(teamId);
        List<UUID> userIds = userTeams.stream()
                .map(UserTeam::getUserId)
                .collect(Collectors.toList());
        
        List<Profile> profiles = profileRepository.findAllById(userIds);
        
        List<TeamMemberResponse.MemberInfo> members = userTeams.stream()
                .map(userTeam -> {
                    Profile profile = profiles.stream()
                            .filter(p -> p.getUserId().equals(userTeam.getUserId()))
                            .findFirst()
                            .orElse(null);
                    if (profile == null) {
                        return null;
                    }
                    return TeamMemberResponse.MemberInfo.builder()
                            .userId(profile.getUserId())
                            .name(profile.getName())
                            .avatarUrl(profile.getAvatarUrl())
                            .joinedAt(userTeam.getJoinedAt())
                            .build();
                })
                .filter(member -> member != null)
                .collect(Collectors.toList());
        
        return TeamMemberResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getName())
                .members(members)
                .build();
    }
    
    public TeamMemberResponse getTeamMembersByCode(TeamCode teamCode) {
        Team team = teamRepository.findByTeamCode(teamCode)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with code: " + teamCode));
        return getTeamMembers(team.getTeamId());
    }
    
    public TeamResponse getTeam(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        
        int memberCount = userTeamRepository.findByTeamId(teamId).size();
        
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .name(team.getName())
                .teamCode(team.getTeamCode())
                .memberCount(memberCount)
                .createdAt(team.getCreatedAt())
                .build();
    }
    
    public TeamResponse getTeamByCode(TeamCode teamCode) {
        Team team = teamRepository.findByTeamCode(teamCode)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with code: " + teamCode));
        return getTeam(team.getTeamId());
    }
    
    public List<TeamResponse> getUserTeams(UUID userId) {
        List<UserTeam> userTeams = userTeamRepository.findByUserId(userId);
        return userTeams.stream()
                .map(userTeam -> {
                    Team team = teamRepository.findById(userTeam.getTeamId())
                            .orElse(null);
                    if (team == null) {
                        return null;
                    }
                    return TeamResponse.builder()
                            .teamId(team.getTeamId())
                            .name(team.getName())
                            .teamCode(team.getTeamCode())
                            .memberCount(userTeamRepository.findByTeamId(team.getTeamId()).size())
                            .createdAt(team.getCreatedAt())
                            .build();
                })
                .filter(team -> team != null)
                .collect(Collectors.toList());
    }
    
    public PageResponse<TeamResponse> getAllTeams(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Team> teams = teamRepository.findAll(pageable);
        
        return PageResponse.<TeamResponse>builder()
                .content(teams.getContent().stream()
                        .map(team -> {
                            int memberCount = userTeamRepository.findByTeamId(team.getTeamId()).size();
                            return TeamResponse.builder()
                                    .teamId(team.getTeamId())
                                    .name(team.getName())
                                    .teamCode(team.getTeamCode())
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
        if (request.getTeamCode() != null && teamRepository.existsByTeamCode(request.getTeamCode())) {
            throw new IllegalArgumentException("Team with code " + request.getTeamCode() + " already exists");
        }
        
        Team team = Team.builder()
                .name(request.getName())
                .teamCode(request.getTeamCode())
                .build();
        
        team = teamRepository.save(team);
        log.info("Team created: {} with code: {}", team.getName(), team.getTeamCode());
        
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .name(team.getName())
                .teamCode(team.getTeamCode())
                .memberCount(0)
                .createdAt(team.getCreatedAt())
                .build();
    }
    
    @Transactional
    public TeamResponse updateTeam(UUID teamId, UpdateTeamRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        
        if (request.getName() != null) {
            team.setName(request.getName());
        }
        if (request.getTeamCode() != null && !request.getTeamCode().equals(team.getTeamCode())) {
            if (teamRepository.existsByTeamCode(request.getTeamCode())) {
                throw new IllegalArgumentException("Team with code " + request.getTeamCode() + " already exists");
            }
            team.setTeamCode(request.getTeamCode());
        }
        team = teamRepository.save(team);
        
        log.info("Team updated: {}", team.getName());
        
        int memberCount = userTeamRepository.findByTeamId(teamId).size();
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .name(team.getName())
                .teamCode(team.getTeamCode())
                .memberCount(memberCount)
                .createdAt(team.getCreatedAt())
                .build();
    }
    
    @Transactional
    public void deleteTeam(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        
        // Remove all user-team mappings
        List<UserTeam> userTeams = userTeamRepository.findByTeamId(teamId);
        userTeamRepository.deleteAll(userTeams);
        
        teamRepository.delete(team);
        log.info("Team deleted: {}", teamId);
    }
    
    @Transactional
    public void addMemberToTeam(UUID teamId, UUID userId) {
        // Validate team exists
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("Team not found: " + teamId);
        }
        
        // Validate profile exists
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));
        
        // Check if user is already a member
        if (userTeamRepository.existsByUserIdAndTeamId(userId, teamId)) {
            log.warn("User {} is already a member of team {}", userId, teamId);
            return;
        }
        
        // Create user-team mapping
        UserTeam userTeam = UserTeam.builder()
                .userId(userId)
                .teamId(teamId)
                .build();
        
        userTeamRepository.save(userTeam);
        
        log.info("User {} added to team {}", userId, teamId);
    }
    
    @Transactional
    public void removeMemberFromTeam(UUID teamId, UUID userId) {
        // Validate team exists
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("Team not found: " + teamId);
        }
        
        UserTeam userTeam = userTeamRepository.findByUserIdAndTeamId(userId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this team"));
        
        userTeamRepository.delete(userTeam);
        
        log.info("User {} removed from team {}", userId, teamId);
    }
}

