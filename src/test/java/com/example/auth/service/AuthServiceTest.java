package com.example.auth.service;

import com.example.auth.dto.AuthResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.UserDto;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.exception.BadRequestException;
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
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

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
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest("testuser", "password");
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_WithDisabledUser_ShouldThrowUnauthorizedException() {
        testUser.setEnabled(false);
        LoginRequest request = new LoginRequest("testuser", "password");
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.of(testUser));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowUnauthorizedException() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void register_WithValidData_ShouldCreateUserWithUserRoleAndEnabled() {
        RegisterRequest request = new RegisterRequest("newuser", "new@example.com", "password123");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto result = authService.register(request);

        assertNotNull(result);
        assertEquals(Role.USER, result.getRole());
        assertTrue(result.isEnabled());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithDuplicateUsername_ShouldThrowBadRequestException() {
        RegisterRequest request = new RegisterRequest("testuser", "new@example.com", "password123");
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    void register_WithDuplicateEmail_ShouldThrowBadRequestException() {
        RegisterRequest request = new RegisterRequest("newuser", "test@example.com", "password123");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }
}
