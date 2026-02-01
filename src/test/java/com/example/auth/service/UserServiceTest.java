package com.example.auth.service;

import com.example.auth.dto.PatchUserRequest;
import com.example.auth.dto.UpdateUserRequest;
import com.example.auth.dto.UserDto;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.exception.BadRequestException;
import com.example.auth.exception.ResourceNotFoundException;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User adminUser;

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

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .password("password")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, adminUser));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        UpdateUserRequest request = new UpdateUserRequest("updateduser", "updated@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto result = userService.updateUser(1L, request);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void disableUser_ShouldSetEnabledToFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertFalse(user.isEnabled());
            return user;
        });

        UserDto result = userService.disableUser(1L);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(userRepository).delete(testUser);
    }

    @Test
    void getCurrentUser_ShouldReturnAuthenticatedUser() {
        mockSecurityContext("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDto result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void updateCurrentUser_WithValidData_ShouldUpdateOwnProfile() {
        mockSecurityContext("testuser");
        UpdateUserRequest request = new UpdateUserRequest("updateduser", "updated@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto result = userService.updateCurrentUser(request);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void patchCurrentUser_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        mockSecurityContext("testuser");
        PatchUserRequest request = new PatchUserRequest("updateduser", null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto result = userService.patchCurrentUser(request);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
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
