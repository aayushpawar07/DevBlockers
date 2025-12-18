package com.devblocker.auth.repository;

import com.devblocker.auth.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByDomain(String domain);
    boolean existsByDomain(String domain);
}

