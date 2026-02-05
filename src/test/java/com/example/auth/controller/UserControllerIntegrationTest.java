package com.example.auth.controller;

import com.example.auth.dto.PatchUserRequest;
import com.example.auth.dto.UpdateUserRequest;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private User adminUser;
    private User otherUser;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = createTestUser("testuser", "test@example.com", "password123", Role.USER, true);
        adminUser = createTestUser("admin", "admin@example.com", "password123", Role.ADMIN, true);
        otherUser = createTestUser("otheruser", "other@example.com", "password123", Role.USER, true);

        entityManager.flush();

        userToken = jwtTokenProvider.generateTokenFromUserId(testUser.getId(), testUser.getRole().name());
        adminToken = jwtTokenProvider.generateTokenFromUserId(adminUser.getId(), adminUser.getRole().name());
    }

    @Test
    void getAllUsers_WithUserRole_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_WithAdminRole_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getUserById_WithUserRole_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_WithAdminRole_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void updateCurrentUser_WithPut_ShouldUpdateOwnProfile() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("updateduser", "updated@example.com");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("updateduser"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"));
    }

    @Test
    void patchCurrentUser_WithPatch_ShouldUpdatePartialProfile() throws Exception {
        PatchUserRequest request = new PatchUserRequest("patcheduser", null);

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("patcheduser"));
    }

    @Test
    void getCurrentUser_ShouldReturnOwnProfile() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void updateUser_WithAdminRole_ShouldUpdateAnyUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("adminupdated", "adminupdated@example.com");

        mockMvc.perform(put("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void disableUser_WithAdminRole_ShouldDisableUser() throws Exception {
        mockMvc.perform(patch("/api/users/" + testUser.getId() + "/disable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void deleteUser_WithAdminRole_ShouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void accessProtectedEndpoint_WithoutToken_ShouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithInvalidToken_ShouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void debugAdminToken() {
        System.out.println("=== DEBUG INFO ===");
        System.out.println("Admin Username: " + adminUser.getUsername());
        System.out.println("Admin Role: " + adminUser.getRole());
        System.out.println("Admin Enabled: " + adminUser.isEnabled());
        System.out.println("Admin Token: " + adminToken);
        
        String extractedUserId = jwtTokenProvider.extractUserId(adminToken);
        System.out.println("Extracted UserId from Token: " + extractedUserId);
        String extractedRole = jwtTokenProvider.extractRole(adminToken);
        System.out.println("Extracted Role from Token: " + extractedRole);
        System.out.println("==================");
    }

    private User createTestUser(String username, String email, String password, Role role, boolean enabled) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .enabled(enabled)
                .build();
        return userRepository.save(user);
    }
}
