package com.example.auth.service;

import com.example.auth.dto.ForgotPasswordRequest;
import com.example.auth.dto.ResetPasswordRequest;
import com.example.auth.dto.ValidateOtpRequest;
import com.example.auth.entity.PasswordResetOtp;
import com.example.auth.entity.User;
import com.example.auth.exception.BadRequestException;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.repository.OtpRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        otpRepository.deleteByEmail(request.getEmail());

        String otpCode = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .email(request.getEmail())
                .otpCode(otpCode)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        otpRepository.save(otp);
        emailService.sendOtp(request.getEmail(), otpCode);
    }

    @Override
    public void validateOtp(ValidateOtpRequest request) {
        PasswordResetOtp otp = otpRepository.findByEmailAndOtpCodeAndUsedFalse(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new BadRequestException("Invalid or already used OTP"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }
        
        // OTP is valid, but don't mark as used (only for validation)
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetOtp otp = otpRepository.findByEmailAndOtpCodeAndUsedFalse(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new BadRequestException("Invalid or already used OTP"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otp.setUsed(true);
        otpRepository.save(otp);
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
