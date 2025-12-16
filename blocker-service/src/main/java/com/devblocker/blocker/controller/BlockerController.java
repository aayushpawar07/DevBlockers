package com.devblocker.blocker.controller;

import com.devblocker.blocker.dto.BlockerResponse;
import com.devblocker.blocker.dto.CreateBlockerRequest;
import com.devblocker.blocker.dto.PageResponse;
import com.devblocker.blocker.dto.ResolveBlockerRequest;
import com.devblocker.blocker.dto.UpdateBestSolutionRequest;
import com.devblocker.blocker.dto.UpdateBlockerRequest;
import com.devblocker.blocker.model.BlockerStatus;
import com.devblocker.blocker.model.Severity;
import com.devblocker.blocker.service.BlockerService;
import com.devblocker.blocker.service.FileStorageService;
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
@RequestMapping("/api/v1/blockers")
@RequiredArgsConstructor
@Tag(name = "Blockers", description = "Blocker management endpoints")
public class BlockerController {
    
    private final BlockerService blockerService;
    private final FileStorageService fileStorageService;
    
    @PostMapping
    @Operation(summary = "Create blocker", description = "Creates a new blocker and publishes BlockerCreated event")
    public ResponseEntity<BlockerResponse> createBlocker(@Valid @RequestBody CreateBlockerRequest request) {
        BlockerResponse response = blockerService.createBlocker(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get blockers", description = "Retrieves blockers with filtering and pagination")
    public ResponseEntity<PageResponse<BlockerResponse>> getBlockers(
            @RequestParam(required = false) BlockerStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) UUID createdBy,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponse<BlockerResponse> response = blockerService.getAllBlockers(
                status, severity, createdBy, assignedTo, teamId, tag, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get blocker", description = "Retrieves a specific blocker by ID")
    public ResponseEntity<BlockerResponse> getBlocker(@PathVariable UUID id) {
        BlockerResponse response = blockerService.getBlocker(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update blocker", description = "Updates a blocker and publishes BlockerUpdated event")
    public ResponseEntity<BlockerResponse> updateBlocker(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBlockerRequest request) {
        BlockerResponse response = blockerService.updateBlocker(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve blocker", description = "Marks a blocker as resolved and publishes BlockerResolved event")
    public ResponseEntity<BlockerResponse> resolveBlocker(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveBlockerRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID resolvedBy) {
        BlockerResponse response = blockerService.resolveBlocker(id, request, resolvedBy);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/best-solution")
    @Operation(summary = "Update best solution", description = "Updates the best solution ID for a blocker without resolving it")
    public ResponseEntity<BlockerResponse> updateBestSolution(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBestSolutionRequest request) {
        BlockerResponse response = blockerService.updateBestSolution(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/upload")
    @Operation(summary = "Upload files", description = "Uploads photos or videos for a blocker")
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
    
    @GetMapping("/files/{filename:.+}")
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
}

