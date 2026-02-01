package com.example.auth.service;

import com.example.auth.dto.PatchUserRequest;
import com.example.auth.dto.UpdateUserRequest;
import com.example.auth.dto.UserDto;

import java.util.List;

public interface UserService {
    
    List<UserDto> getAllUsers();
    
    UserDto getUserById(Long id);
    
    UserDto updateUser(Long id, UpdateUserRequest request);
    
    void deleteUser(Long id);
    
    UserDto disableUser(Long id);
    
    UserDto getCurrentUser();
    
    UserDto updateCurrentUser(UpdateUserRequest request);
    
    UserDto patchCurrentUser(PatchUserRequest request);
}
