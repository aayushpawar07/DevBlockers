package com.devblocker.auth.repository;

import com.devblocker.auth.model.Otp;
import com.devblocker.auth.model.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmailAndCodeAndTypeAndUsedFalse(String email, String code, OtpType type);
    
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE Otp o SET o.used = true WHERE o.email = :email AND o.type = :type AND o.used = false")
    void markAllAsUsed(@Param("email") String email, @Param("type") OtpType type);
}

