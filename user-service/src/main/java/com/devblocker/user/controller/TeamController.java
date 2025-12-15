package com.devblocker.user.controller;

import com.devblocker.user.dto.CreateTeamRequest;
import com.devblocker.user.dto.PageResponse;
import com.devblocker.user.dto.TeamMemberResponse;
import com.devblocker.user.dto.TeamResponse;
import com.devblocker.user.dto.UpdateTeamRequest;
import com.devblocker.user.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team management endpoints")
public class TeamController {
    
    private final TeamService teamService;
    
    @GetMapping
    @Operation(summary = "Get all teams", description = "Retrieves all teams with pagination")
    public ResponseEntity<PageResponse<TeamResponse>> getAllTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TeamResponse> response = teamService.getAllTeams(page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{teamId}")
    @Operation(summary = "Get team", description = "Retrieves a specific team by ID")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable UUID teamId) {
        TeamResponse response = teamService.getTeam(teamId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create team", description = "Creates a new team")
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        TeamResponse response = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{teamId}")
    @Operation(summary = "Update team", description = "Updates an existing team")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody UpdateTeamRequest request) {
        TeamResponse response = teamService.updateTeam(teamId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{teamId}")
    @Operation(summary = "Delete team", description = "Deletes a team and removes all members")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{teamId}/members")
    @Operation(summary = "Get team members", description = "Retrieves all members of a specific team")
    public ResponseEntity<TeamMemberResponse> getTeamMembers(@PathVariable UUID teamId) {
        TeamMemberResponse response = teamService.getTeamMembers(teamId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{teamId}/members/{userId}")
    @Operation(summary = "Add team member", description = "Adds a user to a team")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID teamId,
            @PathVariable UUID userId) {
        teamService.addMemberToTeam(teamId, userId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{teamId}/members/{userId}")
    @Operation(summary = "Remove team member", description = "Removes a user from a team")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID teamId,
            @PathVariable UUID userId) {
        teamService.removeMemberFromTeam(teamId, userId);
        return ResponseEntity.noContent().build();
    }
}

