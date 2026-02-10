package com.example.auth.service;

import com.example.auth.dto.*;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.exception.BadRequestException;
import com.example.auth.exception.UnauthorizedException;
import com.example.auth.mapper.UserMapper;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RedisService redisService;
    private final com.example.auth.client.ApiClient apiClient;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled. Please contact administrator.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );
        } catch (DisabledException e) {
            throw new UnauthorizedException("Account is disabled. Please contact administrator.");
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid username/email or password");
        }

        String token = jwtTokenProvider.generateTokenFromUserId(user.getId(), user.getRole().name());
        
        // Cache user status
        redisService.saveUserStatus(user.getId(), "ACTIVE");
        
        return new AuthResponse(token);
    }

    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        
        redisService.saveUserStatus(savedUser.getId(), "ACTIVE");

        try {
            com.example.auth.dto.InternalEmployeeCreateRequest employeeRequest = new com.example.auth.dto.InternalEmployeeCreateRequest(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    request.getFirstName(),
                    request.getLastName()
            );
            apiClient.post("http://localhost:8081/api/internal/employees", employeeRequest, Void.class);
        } catch (Exception e) {
            log.error("Failed to auto-create employee in HRMS for user {}: {}", savedUser.getId(), e.getMessage(), e);
        }

        return userMapper.toDto(savedUser);
    }
}
