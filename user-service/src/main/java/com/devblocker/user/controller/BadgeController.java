package com.devblocker.user.controller;

import com.devblocker.user.dto.AwardBadgeRequest;
import com.devblocker.user.dto.BadgeResponse;
import com.devblocker.user.dto.CreateBadgeRequest;
import com.devblocker.user.dto.PageResponse;
import com.devblocker.user.dto.UserBadgeResponse;
import com.devblocker.user.service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/badges")
@RequiredArgsConstructor
@Tag(name = "Badges", description = "Badge and achievement management endpoints")
public class BadgeController {
    
    private final BadgeService badgeService;
    
    @GetMapping
    @Operation(summary = "Get all badges", description = "Retrieves all available badges")
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        List<BadgeResponse> badges = badgeService.getAllBadges();
        return ResponseEntity.ok(badges);
    }
    
    @GetMapping("/{badgeId}")
    @Operation(summary = "Get badge", description = "Retrieves a specific badge by ID")
    public ResponseEntity<BadgeResponse> getBadge(@PathVariable UUID badgeId) {
        BadgeResponse response = badgeService.getBadge(badgeId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create badge", description = "Creates a new badge")
    public ResponseEntity<BadgeResponse> createBadge(@Valid @RequestBody CreateBadgeRequest request) {
        BadgeResponse response = badgeService.createBadge(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user badges", description = "Retrieves all badges earned by a user")
    public ResponseEntity<List<UserBadgeResponse>> getUserBadges(@PathVariable UUID userId) {
        List<UserBadgeResponse> badges = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(badges);
    }
    
    @GetMapping("/users/{userId}/paged")
    @Operation(summary = "Get user badges (paged)", description = "Retrieves user badges with pagination")
    public ResponseEntity<PageResponse<UserBadgeResponse>> getUserBadgesPaged(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<UserBadgeResponse> response = badgeService.getUserBadges(userId, 
                org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/users/{userId}/award")
    @Operation(summary = "Award badge", description = "Awards a badge to a user")
    public ResponseEntity<UserBadgeResponse> awardBadge(
            @PathVariable UUID userId,
            @Valid @RequestBody AwardBadgeRequest request) {
        UserBadgeResponse response = badgeService.awardBadge(userId, request.getBadgeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

