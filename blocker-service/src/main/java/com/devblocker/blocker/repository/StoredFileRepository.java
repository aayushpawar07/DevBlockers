package com.devblocker.blocker.repository;

import com.devblocker.blocker.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
    Optional<StoredFile> findByFilename(String filename);
}

