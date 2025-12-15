package com.devblocker.user.repository;

import com.devblocker.user.model.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUserId(UUID userId);
    List<Profile> findByTeamId(UUID teamId);
    
    Page<Profile> findByTeamId(UUID teamId, Pageable pageable);
    
    @Query("SELECT p FROM Profile p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:teamId IS NULL OR p.teamId = :teamId)")
    Page<Profile> searchProfiles(
            @Param("name") String name,
            @Param("teamId") UUID teamId,
            Pageable pageable);
    
    @Query("SELECT p FROM Profile p " +
           "JOIN com.devblocker.user.model.Reputation r ON p.userId = r.userId WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:teamId IS NULL OR p.teamId = :teamId) AND " +
           "(:minReputation IS NULL OR r.points >= :minReputation) AND " +
           "(:maxReputation IS NULL OR r.points <= :maxReputation)")
    Page<Profile> searchProfilesWithReputation(
            @Param("name") String name,
            @Param("teamId") UUID teamId,
            @Param("minReputation") Integer minReputation,
            @Param("maxReputation") Integer maxReputation,
            Pageable pageable);
}

