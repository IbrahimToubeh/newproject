package com.example.auth.service;

import com.example.auth.dto.ForgotPasswordRequest;
import com.example.auth.dto.ResetPasswordRequest;
import com.example.auth.entity.PasswordResetOtp;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.exception.BadRequestException;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.repository.OtpRepository;
import com.example.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OtpServiceImpl otpService;

    private User testUser;
    private PasswordResetOtp testOtp;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();

        testOtp = PasswordResetOtp.builder()
                .id(1L)
                .email("test@example.com")
                .otpCode("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
    }

    @Test
    void requestPasswordReset_WithValidEmail_ShouldGenerateAndSendOtp() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpRepository.save(any(PasswordResetOtp.class))).thenReturn(testOtp);

        otpService.requestPasswordReset(request);

        verify(otpRepository).deleteByEmail("test@example.com");
        verify(otpRepository).save(any(PasswordResetOtp.class));
        verify(emailService).sendOtp(anyString(), anyString());
    }

    @Test
    void requestPasswordReset_WithInvalidEmail_ShouldThrowResourceNotFoundException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("invalid@example.com");
        when(userRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> otpService.requestPasswordReset(request));
    }

    @Test
    void resetPassword_WithValidOtp_ShouldResetPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "newPassword123");
        when(otpRepository.findByEmailAndOtpCodeAndUsedFalse("test@example.com", "123456"))
                .thenReturn(Optional.of(testOtp));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(otpRepository.save(any(PasswordResetOtp.class))).thenReturn(testOtp);

        otpService.resetPassword(request);

        verify(userRepository).save(any(User.class));
        verify(otpRepository).save(argThat(otp -> otp.isUsed()));
    }

    @Test
    void resetPassword_WithExpiredOtp_ShouldThrowBadRequestException() {
        testOtp.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "newPassword123");
        when(otpRepository.findByEmailAndOtpCodeAndUsedFalse("test@example.com", "123456"))
                .thenReturn(Optional.of(testOtp));

        assertThrows(BadRequestException.class, () -> otpService.resetPassword(request));
    }

    @Test
    void resetPassword_WithInvalidOtp_ShouldThrowBadRequestException() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "999999", "newPassword123");
        when(otpRepository.findByEmailAndOtpCodeAndUsedFalse("test@example.com", "999999"))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> otpService.resetPassword(request));
    }

    @Test
    void resetPassword_WithAlreadyUsedOtp_ShouldThrowBadRequestException() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "newPassword123");
        when(otpRepository.findByEmailAndOtpCodeAndUsedFalse("test@example.com", "123456"))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> otpService.resetPassword(request));
    }
}
