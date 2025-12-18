package com.devblocker.auth.service;

import com.devblocker.auth.model.Organization;
import com.devblocker.auth.model.Role;
import com.devblocker.auth.model.User;
import com.devblocker.auth.repository.OrganizationRepository;
import com.devblocker.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public Organization createOrganization(String name, String domain, String adminEmail, String adminPassword, String adminName) {
        // Check if domain already exists
        if (domain != null && !domain.isEmpty() && organizationRepository.existsByDomain(domain)) {
            throw new IllegalArgumentException("Organization domain already exists");
        }
        
        // Check if admin email already exists
        if (userRepository.existsByEmail(adminEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create organization
        Organization organization = Organization.builder()
                .name(name)
                .domain(domain)
                .build();
        
        organization = organizationRepository.save(organization);
        
        // Create organization admin user
        User admin = User.builder()
                .name(adminName)
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ORG_ADMIN)
                .orgId(organization.getOrgId())
                .build();
        
        userRepository.save(admin);
        
        log.info("Organization created: {} with admin: {}", organization.getName(), adminEmail);
        
        return organization;
    }
    
    public Organization getOrganization(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
    }
    
    public Organization getOrganizationByDomain(String domain) {
        return organizationRepository.findByDomain(domain)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
    }
    
    @Transactional
    public User createEmployee(UUID orgId, String name, String email, String password) {
        // Verify organization exists
        if (!organizationRepository.existsById(orgId)) {
            throw new IllegalArgumentException("Organization not found");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        User employee = User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.EMPLOYEE)
                .orgId(orgId)
                .build();
        
        employee = userRepository.save(employee);
        log.info("Employee created: {} in organization: {}", email, orgId);
        
        return employee;
    }
    
    public List<User> getOrganizationEmployees(UUID orgId) {
        return userRepository.findByOrgId(orgId);
    }
}

