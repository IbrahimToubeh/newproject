package com.example.auth.controller;

import com.example.auth.dto.ForgotPasswordRequest;
import com.example.auth.dto.ResetPasswordRequest;
import com.example.auth.dto.ValidateOtpRequest;
import com.example.auth.entity.PasswordResetOtp;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.OtpRepository;
import com.example.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PasswordResetFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        otpRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("oldPassword123"))
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.save(testUser);
    }

    @Test
    void completePasswordResetFlow_ShouldSucceed() throws Exception {
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest("test@example.com");
        
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        PasswordResetOtp otp = otpRepository.findByEmail("test@example.com")
                .orElseThrow();
        String otpCode = otp.getOtpCode();


        ValidateOtpRequest validateRequest = new ValidateOtpRequest("test@example.com", otpCode);
        
        mockMvc.perform(post("/api/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ResetPasswordRequest resetRequest = new ResetPasswordRequest(
                "test@example.com", otpCode, "newPassword123");
        
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void forgotPassword_WithNonExistentEmail_ShouldFail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@example.com");
        
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void validateOtp_WithExpiredOtp_ShouldFail() throws Exception {
        PasswordResetOtp expiredOtp = PasswordResetOtp.builder()
                .email(testUser.getEmail())
                .otpCode("123456")
                .expiresAt(LocalDateTime.now().minusMinutes(10))
                .used(false)
                .build();
        otpRepository.save(expiredOtp);

        ValidateOtpRequest request = new ValidateOtpRequest("test@example.com", "123456");
        
        mockMvc.perform(post("/api/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateOtp_WithAlreadyUsedOtp_ShouldFail() throws Exception {
        PasswordResetOtp usedOtp = PasswordResetOtp.builder()
                .email(testUser.getEmail())
                .otpCode("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(true)
                .build();
        otpRepository.save(usedOtp);

        ValidateOtpRequest request = new ValidateOtpRequest("test@example.com", "123456");
        
        mockMvc.perform(post("/api/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateOtp_WithWrongOtpCode_ShouldFail() throws Exception {
        PasswordResetOtp validOtp = PasswordResetOtp.builder()
                .email(testUser.getEmail())
                .otpCode("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        otpRepository.save(validOtp);

        ValidateOtpRequest request = new ValidateOtpRequest("test@example.com", "999999");
        
        mockMvc.perform(post("/api/auth/validate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_WithInvalidOtp_ShouldFail() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest(
                "test@example.com", "999999", "newPassword123");
        
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_WithInvalidEmail_ShouldFail() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest(
                "invalid-email", "123456", "newPassword123");
        
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_WithShortPassword_ShouldFail() throws Exception {
        PasswordResetOtp validOtp = PasswordResetOtp.builder()
                .email(testUser.getEmail())
                .otpCode("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        otpRepository.save(validOtp);

        ResetPasswordRequest request = new ResetPasswordRequest(
                "test@example.com", "123456", "123");
        
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPassword_WithInvalidEmailFormat_ShouldFail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("not-an-email");
        
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPassword_WithBlankEmail_ShouldFail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("");
        
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
