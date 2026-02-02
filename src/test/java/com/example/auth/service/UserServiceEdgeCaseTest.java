package com.example.auth.service;

import com.example.auth.dto.PatchUserRequest;
import com.example.auth.dto.UpdateUserRequest;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.exception.BadRequestException;
import com.example.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceEdgeCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.example.auth.mapper.UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    void updateUser_WithDuplicateUsername_ShouldThrowBadRequestException() {
        UpdateUserRequest request = new UpdateUserRequest("otheruser", "new@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("otheruser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowBadRequestException() {
        UpdateUserRequest request = new UpdateUserRequest("newusername", "other@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    void updateCurrentUser_WithDuplicateUsername_ShouldThrowBadRequestException() {
        mockSecurityContext("testuser");
        UpdateUserRequest request = new UpdateUserRequest("otheruser", "new@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("otheruser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.updateCurrentUser(request));
    }

    @Test
    void updateCurrentUser_WithDuplicateEmail_ShouldThrowBadRequestException() {
        mockSecurityContext("testuser");
        UpdateUserRequest request = new UpdateUserRequest("newusername", "other@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.updateCurrentUser(request));
    }

    @Test
    void patchCurrentUser_WithDuplicateUsername_ShouldThrowBadRequestException() {
        mockSecurityContext("testuser");
        PatchUserRequest request = new PatchUserRequest("otheruser", null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("otheruser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.patchCurrentUser(request));
    }

    @Test
    void patchCurrentUser_WithDuplicateEmail_ShouldThrowBadRequestException() {
        mockSecurityContext("testuser");
        PatchUserRequest request = new PatchUserRequest(null, "other@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.patchCurrentUser(request));
    }

    @Test
    void getCurrentUser_WithNoAuthentication_ShouldThrowBadRequestException() {
        SecurityContextHolder.clearContext();

        assertThrows(BadRequestException.class, () -> userService.getCurrentUser());
    }

    @Test
    void getCurrentUser_WithNullAuthentication_ShouldThrowBadRequestException() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(BadRequestException.class, () -> userService.getCurrentUser());
    }

    @Test
    void patchCurrentUser_WithOnlyEmail_ShouldUpdateOnlyEmail() {
        mockSecurityContext("testuser");
        PatchUserRequest request = new PatchUserRequest(null, "newemail@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.patchCurrentUser(request);

        verify(userRepository).save(any(User.class));
        verify(userRepository, never()).existsByUsername(anyString());
    }

    private void mockSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
