package com.devblocker.user.repository;

import com.devblocker.user.model.UserTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTeamRepository extends JpaRepository<UserTeam, UUID> {
    
    List<UserTeam> findByUserId(UUID userId);
    
    List<UserTeam> findByTeamId(UUID teamId);
    
    Optional<UserTeam> findByUserIdAndTeamId(UUID userId, UUID teamId);
    
    boolean existsByUserIdAndTeamId(UUID userId, UUID teamId);
    
    @Query("SELECT ut.teamId FROM UserTeam ut WHERE ut.userId = :userId")
    List<UUID> findTeamIdsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT ut.userId FROM UserTeam ut WHERE ut.teamId = :teamId")
    List<UUID> findUserIdsByTeamId(@Param("teamId") UUID teamId);
}

