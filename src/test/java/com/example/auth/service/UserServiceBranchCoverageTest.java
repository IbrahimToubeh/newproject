package com.example.auth.service;

import com.example.auth.dto.PatchUserRequest;
import com.example.auth.dto.UpdateUserRequest;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
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
class UserServiceBranchCoverageTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.example.auth.mapper.UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

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
    }

    @Test
    void updateUser_WithSameUsername_ShouldNotCheckDuplicates() {
        UpdateUserRequest request = new UpdateUserRequest("testuser", "new@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUser(1L, request);

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository).existsByEmail("new@example.com");
    }

    @Test
    void updateUser_WithSameEmail_ShouldNotCheckDuplicates() {
        UpdateUserRequest request = new UpdateUserRequest("newusername", "test@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUser(1L, request);

        verify(userRepository).existsByUsername("newusername");
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateUser_WithBothSameValues_ShouldNotCheckAnyDuplicates() {
        UpdateUserRequest request = new UpdateUserRequest("testuser", "test@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUser(1L, request);

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateUser_WithNullUsername_ShouldOnlyUpdateEmail() {
        UpdateUserRequest request = new UpdateUserRequest(null, "new@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUser(1L, request);

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository).existsByEmail("new@example.com");
    }

    @Test
    void updateUser_WithEmptyUsername_ShouldOnlyUpdateEmail() {
        UpdateUserRequest request = new UpdateUserRequest("", "new@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUser(1L, request);

        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    void updateCurrentUser_WithSameUsername_ShouldNotCheckDuplicates() {
        mockSecurityContext("testuser");
        UpdateUserRequest request = new UpdateUserRequest("testuser", "new@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateCurrentUser(request);

        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    void updateCurrentUser_WithSameEmail_ShouldNotCheckDuplicates() {
        mockSecurityContext("testuser");
        UpdateUserRequest request = new UpdateUserRequest("newusername", "test@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateCurrentUser(request);

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void patchCurrentUser_WithSameUsername_ShouldNotCheckDuplicates() {
        mockSecurityContext("testuser");
        PatchUserRequest request = new PatchUserRequest("testuser", null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.patchCurrentUser(request);

        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    void patchCurrentUser_WithSameEmail_ShouldNotCheckDuplicates() {
        mockSecurityContext("testuser");
        PatchUserRequest request = new PatchUserRequest(null, "test@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.patchCurrentUser(request);

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void patchCurrentUser_WithNullValues_ShouldNotCheckDuplicates() {
        mockSecurityContext("testuser");
        PatchUserRequest request = new PatchUserRequest(null, null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.patchCurrentUser(request);

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void getCurrentUsername_WithAuthenticatedButNotVerified_ShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(Exception.class, () -> userService.getCurrentUser());
    }

    private void mockSecurityContext(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void disableUser_WhenUserAlreadyDisabled_ShouldStillReturnDisabled() {
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.disableUser(1L);

        assertFalse(testUser.isEnabled());
        verify(userRepository).save(testUser);
    }
    
    @Test
    void enableUser_WhenUserAlreadyEnabled_ShouldStillReturnEnabled() {
        testUser.setEnabled(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.enableUser(1L);

        assertTrue(testUser.isEnabled());
        verify(userRepository).save(testUser);
    }
}
