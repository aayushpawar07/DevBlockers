package com.devblocker.user.repository;

import com.devblocker.user.model.Reputation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReputationRepository extends JpaRepository<Reputation, UUID> {
    Optional<Reputation> findByUserId(UUID userId);
    
    @Modifying
    @Query("UPDATE Reputation r SET r.points = r.points + :points WHERE r.userId = :userId")
    void incrementPoints(@Param("userId") UUID userId, @Param("points") Integer points);
}

