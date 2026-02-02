package com.example.auth.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @jakarta.validation.constraints.Pattern(regexp = "^[^@]*$", message = "Username cannot contain '@'")
    private String username;
    
    @Email(message = "Email must be valid")
    private String email;
}
