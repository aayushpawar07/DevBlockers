package com.devblocker.user.controller;

import com.devblocker.user.dto.IncrementReputationRequest;
import com.devblocker.user.dto.PageResponse;
import com.devblocker.user.dto.ProfileResponse;
import com.devblocker.user.dto.ReputationResponse;
import com.devblocker.user.dto.ReputationTransactionResponse;
import com.devblocker.user.dto.UpdateProfileRequest;
import com.devblocker.user.dto.UserBadgeResponse;
import com.devblocker.user.dto.UserSearchRequest;
import com.devblocker.user.service.BadgeService;
import com.devblocker.user.service.ProfileService;
import com.devblocker.user.service.ReputationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and reputation management endpoints")
public class UserController {
    
    private final ProfileService profileService;
    private final ReputationService reputationService;
    private final BadgeService badgeService;
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user profile", description = "Retrieves the profile information for a specific user")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable UUID id) {
        ProfileResponse response = profileService.getProfile(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Updates the profile information for a specific user")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResponse response = profileService.updateProfile(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/reputation")
    @Operation(summary = "Get user reputation", description = "Retrieves the reputation points for a specific user")
    public ResponseEntity<ReputationResponse> getReputation(@PathVariable UUID id) {
        ReputationResponse response = reputationService.getReputation(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/reputation/increment")
    @Operation(summary = "Increment user reputation", description = "Increments the reputation points for a specific user")
    public ResponseEntity<ReputationResponse> incrementReputation(
            @PathVariable UUID id,
            @Valid @RequestBody IncrementReputationRequest request) {
        ReputationResponse response = reputationService.incrementReputation(
                id, request.getPoints(), request.getReason(), request.getSource());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/reputation/history")
    @Operation(summary = "Get reputation history", description = "Retrieves the reputation transaction history for a user with pagination")
    public ResponseEntity<PageResponse<ReputationTransactionResponse>> getReputationHistory(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ReputationTransactionResponse> response = 
                reputationService.getReputationHistory(id, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users with filters and pagination")
    public ResponseEntity<PageResponse<ProfileResponse>> searchUsers(
            @ModelAttribute UserSearchRequest request) {
        PageResponse<ProfileResponse> response = profileService.searchProfiles(
                request.getName(),
                request.getTeamId(),
                request.getMinReputation(),
                request.getMaxReputation(),
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/badges")
    @Operation(summary = "Get user badges", description = "Retrieves all badges earned by a user")
    public ResponseEntity<List<UserBadgeResponse>> getUserBadges(@PathVariable UUID id) {
        List<UserBadgeResponse> badges = badgeService.getUserBadges(id);
        return ResponseEntity.ok(badges);
    }
}

