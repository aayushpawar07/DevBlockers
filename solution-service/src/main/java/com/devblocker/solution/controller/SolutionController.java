package com.devblocker.solution.controller;

import com.devblocker.solution.dto.AcceptSolutionRequest;
import com.devblocker.solution.dto.CreateSolutionRequest;
import com.devblocker.solution.dto.SolutionResponse;
import com.devblocker.solution.dto.SolutionStatsResponse;
import com.devblocker.solution.dto.UpvoteRequest;
import com.devblocker.solution.service.SolutionService;
import com.devblocker.solution.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Solutions", description = "Solution management endpoints for blockers")
public class SolutionController {
    
    private final SolutionService solutionService;
    private final FileStorageService fileStorageService;
    
    @PostMapping("/blockers/{blockerId}/solutions")
    @Operation(summary = "Add solution to blocker", 
               description = "Creates a new solution for a blocker and publishes SolutionAdded event")
    public ResponseEntity<SolutionResponse> addSolution(
            @PathVariable UUID blockerId,
            @Valid @RequestBody CreateSolutionRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String token = extractToken(authHeader);
        SolutionResponse response = solutionService.addSolution(blockerId, request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/blockers/{blockerId}/solutions")
    @Operation(summary = "Get solutions for blocker", 
               description = "Retrieves all solutions for a blocker, ordered by upvotes (desc) and creation date (asc)")
    public ResponseEntity<List<SolutionResponse>> getSolutions(
            @PathVariable UUID blockerId) {
        
        List<SolutionResponse> solutions = solutionService.getSolutionsByBlocker(blockerId);
        return ResponseEntity.ok(solutions);
    }
    
    @PostMapping("/solutions/{solutionId}/upvote")
    @Operation(summary = "Upvote solution", 
               description = "Adds an upvote to a solution (idempotent - one vote per user). Publishes SolutionUpvoted event")
    public ResponseEntity<SolutionResponse> upvoteSolution(
            @PathVariable UUID solutionId,
            @Valid @RequestBody UpvoteRequest request) {
        
        SolutionResponse response = solutionService.upvoteSolution(solutionId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/solutions/{solutionId}/accept")
    @Operation(summary = "Accept solution as best", 
               description = "Marks a solution as accepted (best solution) and updates blocker. Publishes SolutionAccepted event")
    public ResponseEntity<SolutionResponse> acceptSolution(
            @PathVariable UUID solutionId,
            @Valid @RequestBody AcceptSolutionRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String token = extractToken(authHeader);
        SolutionResponse response = solutionService.acceptSolution(solutionId, request, token);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/solutions/{solutionId}")
    @Operation(summary = "Get solution by ID", 
               description = "Retrieves a specific solution by its ID")
    public ResponseEntity<SolutionResponse> getSolution(
            @PathVariable UUID solutionId) {
        
        SolutionResponse response = solutionService.getSolution(solutionId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users/{userId}/solutions/stats")
    @Operation(summary = "Get solution statistics for user", 
               description = "Retrieves solution count and accepted solution count for a user")
    public ResponseEntity<SolutionStatsResponse> getSolutionStats(
            @PathVariable UUID userId) {
        
        long totalSolutions = solutionService.getSolutionCountByUser(userId);
        long acceptedSolutions = solutionService.getAcceptedSolutionCountByUser(userId);
        
        SolutionStatsResponse response = SolutionStatsResponse.builder()
                .totalSolutions(totalSolutions)
                .acceptedSolutions(acceptedSolutions)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/solutions/upload")
    @Operation(summary = "Upload files", description = "Uploads photos or videos for a solution")
    public ResponseEntity<Map<String, List<String>>> uploadFiles(
            @RequestParam("files") MultipartFile[] files) {
        try {
            List<String> fileUrls = fileStorageService.storeFiles(files);
            Map<String, List<String>> response = new HashMap<>();
            response.put("fileUrls", fileUrls);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, List<String>> errorResponse = new HashMap<>();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @GetMapping("/solutions/files/{filename:.+}")
    @Operation(summary = "Get file", description = "Retrieves an uploaded file")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path filePath = fileStorageService.loadFile(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private String determineContentType(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "application/octet-stream";
        }
        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "mov" -> "video/quicktime";
            case "avi" -> "video/x-msvideo";
            default -> "application/octet-stream";
        };
    }
    
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

