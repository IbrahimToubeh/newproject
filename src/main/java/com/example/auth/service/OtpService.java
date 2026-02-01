package com.example.auth.service;

import com.example.auth.dto.ForgotPasswordRequest;
import com.example.auth.dto.ResetPasswordRequest;
import com.example.auth.dto.ValidateOtpRequest;

public interface OtpService {
    
    void requestPasswordReset(ForgotPasswordRequest request);
    
    void validateOtp(ValidateOtpRequest request);
    
    void resetPassword(ResetPasswordRequest request);
}
