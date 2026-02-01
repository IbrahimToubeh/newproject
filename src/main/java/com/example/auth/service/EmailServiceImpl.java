package com.example.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendOtp(String email, String otpCode) {
        log.info("=================================================");
        log.info("MOCK EMAIL SERVICE - OTP EMAIL");
        log.info("=================================================");
        log.info("To: {}", email);
        log.info("Subject: Password Reset OTP");
        log.info("Body: Your OTP code is: {}", otpCode);
        log.info("This code will expire in 5 minutes.");
        log.info("=================================================");
    }
}
