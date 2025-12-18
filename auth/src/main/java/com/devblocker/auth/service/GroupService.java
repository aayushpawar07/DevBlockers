package com.devblocker.auth.service;

import com.devblocker.auth.model.Group;
import com.devblocker.auth.model.GroupMember;
import com.devblocker.auth.model.User;
import com.devblocker.auth.repository.GroupMemberRepository;
import com.devblocker.auth.repository.GroupRepository;
import com.devblocker.auth.repository.OrganizationRepository;
import com.devblocker.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public Group createGroup(UUID orgId, String name, String description) {
        // Verify organization exists
        if (!organizationRepository.existsById(orgId)) {
            throw new IllegalArgumentException("Organization not found");
        }
        
        // Check if group name already exists in organization
        if (groupRepository.existsByOrgIdAndName(orgId, name)) {
            throw new IllegalArgumentException("Group name already exists in this organization");
        }
        
        Group group = Group.builder()
                .orgId(orgId)
                .name(name)
                .description(description)
                .build();
        
        group = groupRepository.save(group);
        log.info("Group created: {} in organization: {}", name, orgId);
        
        return group;
    }
    
    public List<Group> getGroupsByOrganization(UUID orgId) {
        return groupRepository.findByOrgId(orgId);
    }
    
    public Group getGroup(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }
    
    @Transactional
    public void addMemberToGroup(UUID groupId, UUID userId) {
        // Verify group exists
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        // Verify user exists and belongs to same organization
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getOrgId() == null || !user.getOrgId().equals(group.getOrgId())) {
            throw new IllegalArgumentException("User does not belong to the same organization");
        }
        
        // Check if already a member
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new IllegalArgumentException("User is already a member of this group");
        }
        
        GroupMember member = GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .build();
        
        groupMemberRepository.save(member);
        log.info("User {} added to group {}", userId, groupId);
    }
    
    @Transactional
    public void removeMemberFromGroup(UUID groupId, UUID userId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this group"));
        
        groupMemberRepository.delete(member);
        log.info("User {} removed from group {}", userId, groupId);
    }
    
    public List<UUID> getUserGroupIds(UUID userId) {
        return groupMemberRepository.findGroupIdsByUserId(userId);
    }
    
    public List<GroupMember> getGroupMembers(UUID groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }
}

