package com.devblocker.auth.controller;

import com.devblocker.auth.dto.CreateEmployeeRequest;
import com.devblocker.auth.dto.OrganizationRegisterRequest;
import com.devblocker.auth.dto.OrganizationResponse;
import com.devblocker.auth.dto.UserResponse;
import com.devblocker.auth.model.Organization;
import com.devblocker.auth.model.Role;
import com.devblocker.auth.model.User;
import com.devblocker.auth.service.JwtService;
import com.devblocker.auth.service.OrganizationService;
import com.devblocker.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization management endpoints")
public class OrganizationController {
    
    private final OrganizationService organizationService;
    private final UserService userService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new organization", description = "Creates a new organization with an admin account")
    public ResponseEntity<OrganizationResponse> register(@Valid @RequestBody OrganizationRegisterRequest request) {
        Organization organization = organizationService.createOrganization(
                request.getOrganizationName(),
                request.getDomain(),
                request.getAdminEmail(),
                request.getAdminPassword(),
                request.getAdminName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(OrganizationResponse.fromEntity(organization));
    }
    
    @PostMapping("/{orgId}/employees")
    @Operation(summary = "Create employee", description = "Creates a new employee account in the organization (ORG_ADMIN only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<UserResponse> createEmployee(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateEmployeeRequest request,
            Authentication authentication) {
        
        // Verify the authenticated user is an admin of this organization
        User admin = userService.getUserEntityByEmail(authentication.getName());
        
        if (admin.getRole() != Role.ORG_ADMIN || !orgId.equals(admin.getOrgId())) {
            throw new IllegalArgumentException("Unauthorized: Only organization admins can create employees");
        }
        
        User employee = organizationService.createEmployee(
                orgId,
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToUserResponse(employee));
    }
    
    @GetMapping("/{orgId}/employees")
    @Operation(summary = "Get organization employees", description = "Returns all employees in the organization")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<UserResponse>> getEmployees(
            @PathVariable UUID orgId,
            Authentication authentication) {
        
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Authentication required. Please login again.");
        }
        
        // Verify user belongs to the organization
        User user = userService.getUserEntityByEmail(authentication.getName());
        
        if (user.getOrgId() == null || !orgId.equals(user.getOrgId())) {
            throw new IllegalArgumentException("Unauthorized: User does not belong to this organization");
        }
        
        List<User> employees = organizationService.getOrganizationEmployees(orgId);
        List<UserResponse> responses = employees.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{orgId}")
    @Operation(summary = "Get organization", description = "Returns organization details")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<OrganizationResponse> getOrganization(
            @PathVariable UUID orgId,
            Authentication authentication) {
        
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Authentication required. Please login again.");
        }
        
        // Verify user belongs to the organization
        User user = userService.getUserEntityByEmail(authentication.getName());
        
        if (user.getOrgId() == null || !orgId.equals(user.getOrgId())) {
            throw new IllegalArgumentException("Unauthorized: User does not belong to this organization");
        }
        
        Organization organization = organizationService.getOrganization(orgId);
        return ResponseEntity.ok(OrganizationResponse.fromEntity(organization));
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .orgId(user.getOrgId())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

