package com.devblocker.solution.repository;

import com.devblocker.solution.model.SolutionUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolutionUpvoteRepository extends JpaRepository<SolutionUpvote, UUID> {
    
    Optional<SolutionUpvote> findBySolutionIdAndUserId(UUID solutionId, UUID userId);
    
    boolean existsBySolutionIdAndUserId(UUID solutionId, UUID userId);
    
    long countBySolutionId(UUID solutionId);
}

