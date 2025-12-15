package com.devblocker.blocker.service;

import com.devblocker.blocker.model.Blocker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DuplicateDetectionService {
    
    @Value("${search.service.url:http://localhost:8084}")
    private String searchServiceUrl;
    
    @Value("${duplicate.detection.enabled:false}")
    private boolean duplicateDetectionEnabled;
    
    public void checkDuplicates(Blocker blocker) {
        if (!duplicateDetectionEnabled) {
            log.debug("Duplicate detection is disabled. Skipping check for blocker: {}", blocker.getBlockerId());
            return;
        }
        
        try {
            // Stub: Call search-service to check for duplicates
            // This will be implemented when search-service is available
            Map<String, Object> request = new HashMap<>();
            request.put("title", blocker.getTitle());
            request.put("description", blocker.getDescription());
            request.put("blockerId", blocker.getBlockerId().toString());
            
            // TODO: Implement actual call to search-service
            // String url = searchServiceUrl + "/api/v1/search/duplicates";
            // DuplicateCheckResponse response = restTemplate.postForObject(url, request, DuplicateCheckResponse.class);
            
            log.info("Duplicate check triggered for blocker: {} (stub implementation)", blocker.getBlockerId());
            
            // For now, just log that duplicate check was triggered
            // In production, this would call the search-service API
            
        } catch (Exception e) {
            log.error("Failed to check duplicates for blocker: {}", blocker.getBlockerId(), e);
            // Don't throw - duplicate detection failure shouldn't break blocker creation
        }
    }
}

