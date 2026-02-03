package hrms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponseDTO(
        Long id,
        Long userId,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String department,
        String position,
        LocalDate dateOfBirth,
        LocalDate joiningDate,
        String address,
        String status,
        LocalDateTime createdAt
) {}
