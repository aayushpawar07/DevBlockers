package com.devblocker.solution.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {
    
    private final Path fileStorageLocation;
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final List<String> ALLOWED_VIDEO_TYPES = List.of(
            "video/mp4", "video/webm", "video/quicktime", "video/x-msvideo"
    );
    
    public FileStorageService(@Value("${file.upload-dir:uploads/solutions}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage location: {}", this.fileStorageLocation);
        } catch (IOException e) {
            log.error("Could not create the directory where the uploaded files will be stored.", e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
    
    public List<String> storeFiles(MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            
            try {
                String fileUrl = storeFile(file);
                fileUrls.add(fileUrl);
            } catch (Exception e) {
                log.error("Failed to store file: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
            }
        }
        
        return fileUrls;
    }
    
    public String storeFile(MultipartFile file) {
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 50MB");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || 
            (!ALLOWED_IMAGE_TYPES.contains(contentType) && !ALLOWED_VIDEO_TYPES.contains(contentType))) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: images (JPEG, PNG, GIF, WEBP) and videos (MP4, WEBM, MOV, AVI)");
        }
        
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // Copy file to target location
            Path targetLocation = this.fileStorageLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Return URL path (relative to serve from static resources)
            String fileUrl = "/api/v1/solutions/files/" + filename;
            log.info("File stored successfully: {}", fileUrl);
            
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to store file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }
    
    public Path loadFile(String filename) {
        return fileStorageLocation.resolve(filename).normalize();
    }
}

