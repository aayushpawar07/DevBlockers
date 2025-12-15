package com.devblocker.solution.repository;

import com.devblocker.solution.model.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, UUID> {
    
    List<Solution> findByBlockerIdOrderByUpvotesDescCreatedAtAsc(UUID blockerId);
    
    List<Solution> findByBlockerIdAndAcceptedTrue(UUID blockerId);
    
    Optional<Solution> findBySolutionId(UUID solutionId);
    
    boolean existsByBlockerIdAndAcceptedTrue(UUID blockerId);
    
    long countByBlockerId(UUID blockerId);
    
    List<Solution> findByUserId(UUID userId);
    
    long countByUserId(UUID userId);
    
    long countByUserIdAndAcceptedTrue(UUID userId);
}

