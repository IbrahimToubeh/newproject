package com.example.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registerRequest_WithValidData_ShouldPass() {
        RegisterRequest request = new RegisterRequest("username", "test@example.com", "password123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequest_WithBlankUsername_ShouldFail() {
        RegisterRequest request = new RegisterRequest("", "test@example.com", "password123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void registerRequest_WithInvalidEmail_ShouldFail() {
        RegisterRequest request = new RegisterRequest("username", "invalid-email", "password123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void registerRequest_WithShortPassword_ShouldFail() {
        RegisterRequest request = new RegisterRequest("username", "test@example.com", "123");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void loginRequest_WithValidData_ShouldPass() {
        LoginRequest request = new LoginRequest("username", "password123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequest_WithBlankUsernameOrEmail_ShouldFail() {
        LoginRequest request = new LoginRequest("", "password123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void loginRequest_WithBlankPassword_ShouldFail() {
        LoginRequest request = new LoginRequest("username", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void updateUserRequest_WithValidData_ShouldPass() {
        UpdateUserRequest request = new UpdateUserRequest("newusername", "newemail@example.com");
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void updateUserRequest_WithInvalidEmail_ShouldFail() {
        UpdateUserRequest request = new UpdateUserRequest("username", "invalid-email");
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void forgotPasswordRequest_WithValidEmail_ShouldPass() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void forgotPasswordRequest_WithInvalidEmail_ShouldFail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("invalid-email");
        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void resetPasswordRequest_WithValidData_ShouldPass() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "newPassword123");
        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void resetPasswordRequest_WithShortPassword_ShouldFail() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "123");
        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void resetPasswordRequest_WithBlankOtp_ShouldFail() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "", "newPassword123");
        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validateOtpRequest_WithValidData_ShouldPass() {
        ValidateOtpRequest request = new ValidateOtpRequest("test@example.com", "123456");
        Set<ConstraintViolation<ValidateOtpRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void validateOtpRequest_WithBlankOtp_ShouldFail() {
        ValidateOtpRequest request = new ValidateOtpRequest("test@example.com", "");
        Set<ConstraintViolation<ValidateOtpRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
