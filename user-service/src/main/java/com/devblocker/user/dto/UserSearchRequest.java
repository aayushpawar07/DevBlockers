package com.devblocker.user.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserSearchRequest {
    private String name;
    private UUID teamId;
    private Integer minReputation;
    private Integer maxReputation;
    private Integer page = 0;
    private Integer size = 20;
}

