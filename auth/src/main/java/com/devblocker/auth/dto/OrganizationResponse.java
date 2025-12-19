package com.devblocker.auth.dto;

import com.devblocker.auth.model.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private UUID orgId;
    private String name;
    private String domain;
    private LocalDateTime createdAt;
    
    public static OrganizationResponse fromEntity(Organization organization) {
        return OrganizationResponse.builder()
                .orgId(organization.getOrgId())
                .name(organization.getName())
                .domain(organization.getDomain())
                .createdAt(organization.getCreatedAt())
                .build();
    }
}

