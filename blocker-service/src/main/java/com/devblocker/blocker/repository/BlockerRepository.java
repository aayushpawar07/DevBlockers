package com.devblocker.blocker.repository;

import com.devblocker.blocker.model.Blocker;
import com.devblocker.blocker.model.BlockerStatus;
import com.devblocker.blocker.model.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlockerRepository extends JpaRepository<Blocker, UUID> {
    
    Optional<Blocker> findByBlockerId(UUID blockerId);
    
    Page<Blocker> findByStatus(BlockerStatus status, Pageable pageable);
    
    Page<Blocker> findBySeverity(Severity severity, Pageable pageable);
    
    Page<Blocker> findByCreatedBy(UUID createdBy, Pageable pageable);
    
    Page<Blocker> findByAssignedTo(UUID assignedTo, Pageable pageable);
    
    Page<Blocker> findByTeamId(UUID teamId, Pageable pageable);
    
    @Query("SELECT b FROM Blocker b WHERE :tag MEMBER OF b.tags")
    Page<Blocker> findByTag(@Param("tag") String tag, Pageable pageable);
    
    @Query("SELECT b FROM Blocker b WHERE " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:severity IS NULL OR b.severity = :severity) AND " +
           "(:createdBy IS NULL OR b.createdBy = :createdBy) AND " +
           "(:assignedTo IS NULL OR b.assignedTo = :assignedTo) AND " +
           "(:teamId IS NULL OR b.teamId = :teamId) AND " +
           "(:tag IS NULL OR :tag MEMBER OF b.tags) AND " +
           "(b.visibility = 'PUBLIC' OR " +
           " (b.visibility = 'ORG' AND (:userOrgId IS NOT NULL AND b.orgId = :userOrgId)) OR " +
           " (b.visibility = 'GROUP' AND (:userGroupIds IS NOT NULL AND b.groupId IN :userGroupIds)))")
    Page<Blocker> findWithFilters(
            @Param("status") BlockerStatus status,
            @Param("severity") Severity severity,
            @Param("createdBy") UUID createdBy,
            @Param("assignedTo") UUID assignedTo,
            @Param("teamId") UUID teamId,
            @Param("tag") String tag,
            @Param("userOrgId") UUID userOrgId,
            @Param("userGroupIds") java.util.List<UUID> userGroupIds,
            Pageable pageable);
    
    @Query("SELECT b FROM Blocker b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Blocker> searchByText(@Param("query") String query, Pageable pageable);
}

