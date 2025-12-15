package com.devblocker.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReplyRequest {
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
}

