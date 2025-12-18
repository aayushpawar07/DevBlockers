package com.devblocker.auth.repository;

import com.devblocker.auth.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByOrgId(UUID orgId);
    boolean existsByOrgIdAndName(UUID orgId, String name);
}

