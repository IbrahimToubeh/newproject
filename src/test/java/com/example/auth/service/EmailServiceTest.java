package com.example.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendOtp_ShouldNotThrowException() {
        assertDoesNotThrow(() -> 
            emailService.sendOtp("test@example.com", "123456"));
    }

    @Test
    void sendOtp_WithNullEmail_ShouldNotThrowException() {
        assertDoesNotThrow(() -> 
            emailService.sendOtp(null, "123456"));
    }

    @Test
    void sendOtp_WithNullOtp_ShouldNotThrowException() {
        assertDoesNotThrow(() -> 
            emailService.sendOtp("test@example.com", null));
    }

    @Test
    void sendOtp_WithEmptyEmail_ShouldNotThrowException() {
        assertDoesNotThrow(() -> 
            emailService.sendOtp("", "123456"));
    }

    @Test
    void sendOtp_WithEmptyOtp_ShouldNotThrowException() {
        assertDoesNotThrow(() -> 
            emailService.sendOtp("test@example.com", ""));
    }
}
