package com.example.auth.service;

import com.example.auth.dto.PageResponse;
import com.example.auth.dto.PatchUserRequest;
import com.example.auth.dto.UpdateUserRequest;
import com.example.auth.dto.UserDto;
import com.example.auth.entity.User;
import com.example.auth.exception.BadRequestException;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.mapper.UserMapper;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RedisService redisService;
    private final com.example.auth.client.ApiClient apiClient;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> getAllUsers(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<User> users = userRepository.findAll(pageable);
        List<UserDto> content = users.getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        return PageResponse.<UserDto>builder()
                .content(content)
                .pageNo(users.getNumber())
                .pageSize(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .last(users.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        log.info("Updating user {}: current email={}, request email={}", id, user.getEmail(), request.getEmail());

        if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }

        boolean emailChanged = false;

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
            emailChanged = true;
        }

        User updatedUser = userRepository.save(user);
        
        if (emailChanged) {
            try {
                // Sync email to HRMS Service
                apiClient.patch("http://localhost:8081/api/internal/employees/" + updatedUser.getId() + "/email?email=" + updatedUser.getEmail(), null, Void.class);
            } catch (Exception e) {
                log.error("Failed to sync email to HRMS Service for user {}: {}", updatedUser.getId(), e.getMessage(), e);
            }
        }
        
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
        redisService.deleteUserStatus(id);
    }

    @Override
    @Transactional
    public UserDto disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setEnabled(false);
        User updatedUser = userRepository.save(user);
        redisService.saveUserStatus(id, "DISABLED");
        
        try {
            apiClient.patch("http://localhost:8081/api/internal/employees/" + id + "/status?status=INACTIVE", null, Void.class);
        } catch (Exception e) {
            log.error("Failed to update employee status in HRMS: {}", e.getMessage());
        }
        
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDto enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setEnabled(true);
        User updatedUser = userRepository.save(user);
        redisService.saveUserStatus(id, "ACTIVE");
        
        try {
            apiClient.patch("http://localhost:8081/api/internal/employees/" + id + "/status?status=ACTIVE", null, Void.class);
        } catch (Exception e) {
            log.error("Failed to update employee status in HRMS: {}", e.getMessage());
        }
        
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateCurrentUser(UpdateUserRequest request) {
        String currentUsername = getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(currentUser.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already exists");
            }
            currentUser.setUsername(request.getUsername());
        }

        boolean emailChanged = false;

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(currentUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            currentUser.setEmail(request.getEmail());
            emailChanged = true;
        }

        User updatedUser = userRepository.save(currentUser);
        
        if (emailChanged) {
            try {
                // Sync email to HRMS Service
                apiClient.patch("http://localhost:8081/api/internal/employees/" + updatedUser.getId() + "/email?email=" + updatedUser.getEmail(), null, Void.class);
            } catch (Exception e) {
                log.error("Failed to sync email to HRMS Service for user {}: {}", updatedUser.getId(), e.getMessage(), e);
            }
        }
        
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDto patchCurrentUser(PatchUserRequest request) {
        String currentUsername = getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(currentUser.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already exists");
            }
            currentUser.setUsername(request.getUsername());
        }

        boolean emailChanged = false;

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(currentUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            currentUser.setEmail(request.getEmail());
            emailChanged = true;
        }

        User updatedUser = userRepository.save(currentUser);
        
        if (emailChanged) {
             try {
                // Sync email to HRMS Service
                apiClient.patch("http://localhost:8081/api/internal/employees/" + updatedUser.getId() + "/email?email=" + updatedUser.getEmail(), null, Void.class);
            } catch (Exception e) {
                log.error("Failed to sync email to HRMS Service for user {}: {}", updatedUser.getId(), e.getMessage(), e);
            }
        }
        
        return userMapper.toDto(updatedUser);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("No authenticated user found");
        }
        return authentication.getName();
    }

    @Override
    @Transactional
    public void updateUserEmailInternal(Long userId, String email) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setEmail(email);
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void updateUserStatusInternal(Long userId, boolean enabled) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setEnabled(enabled);
            userRepository.save(user);
            redisService.saveUserStatus(userId, enabled ? "ACTIVE" : "DISABLED");
        });
    }


}
