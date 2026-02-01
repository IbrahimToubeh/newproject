package com.example.auth.repository;

import com.example.auth.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    
    Optional<PasswordResetOtp> findByEmailAndOtpCodeAndUsedFalse(String email, String otpCode);
    
    Optional<PasswordResetOtp> findByEmail(String email);
    
    void deleteByEmail(String email);
}
