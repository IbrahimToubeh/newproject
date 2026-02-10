package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalEmployeeCreateRequest {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
}
