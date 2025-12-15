package com.devblocker.user.repository;

import com.devblocker.user.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, UUID> {
    Optional<Badge> findByBadgeId(UUID badgeId);
    Optional<Badge> findByName(String name);
}

