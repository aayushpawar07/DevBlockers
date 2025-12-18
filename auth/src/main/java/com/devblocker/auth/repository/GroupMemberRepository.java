package com.devblocker.auth.repository;

import com.devblocker.auth.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    List<GroupMember> findByUserId(UUID userId);
    List<GroupMember> findByGroupId(UUID groupId);
    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);
    
    @Query("SELECT gm.groupId FROM GroupMember gm WHERE gm.userId = :userId")
    List<UUID> findGroupIdsByUserId(@Param("userId") UUID userId);
}

