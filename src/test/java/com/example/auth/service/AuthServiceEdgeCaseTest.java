package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.exception.UnauthorizedException;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceEdgeCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private com.example.auth.mapper.UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    void login_WhenAuthenticationFailsWithBadCredentials_ShouldThrowUnauthorizedException() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void login_WithDisabledUserBeforeAuthentication_ShouldThrowUnauthorizedException() {
        testUser.setEnabled(false);
        LoginRequest request = new LoginRequest("testuser", "password");
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(testUser));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, 
                () -> authService.login(request));
        
        assertTrue(exception.getMessage().contains("disabled"));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowUnauthorizedException() {
        LoginRequest request = new LoginRequest("nonexistent", "password");
        when(userRepository.findByUsernameOrEmail("nonexistent", "nonexistent")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        verify(authenticationManager, never()).authenticate(any());
    }
}
