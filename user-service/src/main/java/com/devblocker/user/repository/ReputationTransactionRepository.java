package com.devblocker.user.repository;

import com.devblocker.user.model.ReputationTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ReputationTransactionRepository extends JpaRepository<ReputationTransaction, UUID> {
    Page<ReputationTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    @Query("SELECT rt FROM ReputationTransaction rt WHERE rt.userId = :userId " +
           "AND rt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY rt.createdAt DESC")
    Page<ReputationTransaction> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(rt.points), 0) FROM ReputationTransaction rt WHERE rt.userId = :userId")
    Integer getTotalReputationPoints(@Param("userId") UUID userId);
}

