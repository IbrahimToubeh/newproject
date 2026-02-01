package com.example.auth.service;

import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.UserDto;

public interface AuthService {
    
    AuthResponse login(LoginRequest request);
    
    UserDto register(RegisterRequest request);
}
