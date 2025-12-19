package com.devblocker.auth.dto;

import com.devblocker.auth.model.Role;
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
public class UserResponse {
    private UUID userId;
    private String name;
    private String email;
    private Role role;
    private UUID orgId;
    private LocalDateTime createdAt;
}

