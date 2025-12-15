package com.devblocker.user.repository;

import com.devblocker.user.model.UserBadge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {
    List<UserBadge> findByUserId(UUID userId);
    Page<UserBadge> findByUserId(UUID userId, Pageable pageable);
    Optional<UserBadge> findByUserIdAndBadgeId(UUID userId, UUID badgeId);
    boolean existsByUserIdAndBadgeId(UUID userId, UUID badgeId);
}

