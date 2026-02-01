package com.example.auth.service;

public interface EmailService {
    
    void sendOtp(String email, String otpCode);
}
