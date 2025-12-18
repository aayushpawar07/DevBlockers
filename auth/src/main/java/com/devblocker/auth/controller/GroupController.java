package com.devblocker.auth.controller;

import com.devblocker.auth.dto.CreateGroupRequest;
import com.devblocker.auth.model.Group;
import com.devblocker.auth.model.GroupMember;
import com.devblocker.auth.model.Role;
import com.devblocker.auth.model.User;
import com.devblocker.auth.service.GroupService;
import com.devblocker.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Group management endpoints")
public class GroupController {
    
    private final GroupService groupService;
    private final UserService userService;
    
    @PostMapping
    @Operation(summary = "Create group", description = "Creates a new group in the organization (ORG_ADMIN only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<GroupResponse> createGroup(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication) {
        
        // Verify the authenticated user is an admin of this organization
        User admin = userService.getUserEntityByEmail(authentication.getName());
        
        if (admin.getRole() != Role.ORG_ADMIN || !orgId.equals(admin.getOrgId())) {
            throw new IllegalArgumentException("Unauthorized: Only organization admins can create groups");
        }
        
        Group group = groupService.createGroup(orgId, request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.fromEntity(group));
    }
    
    @GetMapping
    @Operation(summary = "Get organization groups", description = "Returns all groups in the organization")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<GroupResponse>> getGroups(
            @PathVariable UUID orgId,
            Authentication authentication) {
        
        // Verify user belongs to the organization
        User user = userService.getUserEntityByEmail(authentication.getName());
        
        if (user.getOrgId() == null || !orgId.equals(user.getOrgId())) {
            throw new IllegalArgumentException("Unauthorized: User does not belong to this organization");
        }
        
        List<Group> groups = groupService.getGroupsByOrganization(orgId);
        List<GroupResponse> responses = groups.stream()
                .map(GroupResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Add member to group", description = "Adds an employee to a group (ORG_ADMIN only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID orgId,
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            Authentication authentication) {
        
        // Verify the authenticated user is an admin of this organization
        User admin = userService.getUserEntityByEmail(authentication.getName());
        
        if (admin.getRole() != Role.ORG_ADMIN || !orgId.equals(admin.getOrgId())) {
            throw new IllegalArgumentException("Unauthorized: Only organization admins can add members");
        }
        
        groupService.addMemberToGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Remove member from group", description = "Removes an employee from a group (ORG_ADMIN only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID orgId,
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            Authentication authentication) {
        
        // Verify the authenticated user is an admin of this organization
        User admin = userService.getUserEntityByEmail(authentication.getName());
        
        if (admin.getRole() != Role.ORG_ADMIN || !orgId.equals(admin.getOrgId())) {
            throw new IllegalArgumentException("Unauthorized: Only organization admins can remove members");
        }
        
        groupService.removeMemberFromGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{groupId}/members")
    @Operation(summary = "Get group members", description = "Returns all members of a group")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(
            @PathVariable UUID orgId,
            @PathVariable UUID groupId,
            Authentication authentication) {
        
        // Verify user belongs to the organization
        User user = userService.getUserEntityByEmail(authentication.getName());
        
        if (user.getOrgId() == null || !orgId.equals(user.getOrgId())) {
            throw new IllegalArgumentException("Unauthorized: User does not belong to this organization");
        }
        
        List<GroupMember> members = groupService.getGroupMembers(groupId);
        List<GroupMemberResponse> responses = members.stream()
                .map(member -> {
                    User memberUser = userService.getUserEntityById(member.getUserId());
                    return GroupMemberResponse.builder()
                            .memberId(member.getMemberId())
                            .groupId(member.getGroupId())
                            .userId(member.getUserId())
                            .userName(memberUser.getName())
                            .userEmail(memberUser.getEmail())
                            .joinedAt(member.getJoinedAt())
                            .build();
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupResponse {
        private UUID groupId;
        private UUID orgId;
        private String name;
        private String description;
        private LocalDateTime createdAt;
        
        public static GroupResponse fromEntity(Group group) {
            return GroupResponse.builder()
                    .groupId(group.getGroupId())
                    .orgId(group.getOrgId())
                    .name(group.getName())
                    .description(group.getDescription())
                    .createdAt(group.getCreatedAt())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupMemberResponse {
        private UUID memberId;
        private UUID groupId;
        private UUID userId;
        private String userName;
        private String userEmail;
        private LocalDateTime joinedAt;
    }
}

