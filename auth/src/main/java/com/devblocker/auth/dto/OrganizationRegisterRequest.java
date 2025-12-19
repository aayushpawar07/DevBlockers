package com.devblocker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrganizationRegisterRequest {
    
    @NotBlank(message = "Organization name is required")
    private String organizationName;
    
    private String domain;
    
    @NotBlank(message = "Admin name is required")
    private String adminName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String adminEmail;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String adminPassword;
}

