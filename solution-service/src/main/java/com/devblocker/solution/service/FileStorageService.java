package com.devblocker.solution.service;

import com.devblocker.solution.model.StoredFile;
import com.devblocker.solution.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    private final StoredFileRepository storedFileRepository;
    
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final List<String> ALLOWED_VIDEO_TYPES = List.of(
            "video/mp4", "video/webm", "video/quicktime", "video/x-msvideo"
    );
    
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
            if (originalFilename == null) {
                originalFilename = "file";
            }
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // Read file data into byte array
            byte[] fileData = file.getBytes();
            
            // Store file in database
            StoredFile storedFile = StoredFile.builder()
                    .filename(filename)
                    .originalFilename(originalFilename)
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .fileData(fileData)
                    .build();
            
            storedFile = storedFileRepository.save(storedFile);
            
            // Return URL path using file ID
            String fileUrl = "/api/v1/solutions/files/" + storedFile.getFileId();
            log.info("File stored successfully in database: {} (ID: {})", fileUrl, storedFile.getFileId());
            
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to store file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }
    
    public StoredFile loadFile(UUID fileId) {
        return storedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));
    }
    
    public StoredFile loadFileByFilename(String filename) {
        return storedFileRepository.findByFilename(filename)
                .orElseThrow(() -> new RuntimeException("File not found with filename: " + filename));
    }
}

