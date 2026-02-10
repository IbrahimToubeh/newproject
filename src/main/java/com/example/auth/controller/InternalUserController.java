package com.example.auth.controller;

import com.example.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @PatchMapping("/{userId}/email")
    public ResponseEntity<Void> updateUserEmail(@PathVariable Long userId, @RequestParam String email) {
        userService.updateUserEmailInternal(userId, email);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long userId, @RequestParam boolean enabled) {
        userService.updateUserStatusInternal(userId, enabled);
        return ResponseEntity.noContent().build();
    }
}
