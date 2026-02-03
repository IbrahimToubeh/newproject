package hrms.dto;


import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateEmployeeRequestDTO(

        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Size(max = 20)
        String phoneNumber,

        @NotBlank(message = "Department is required")
        @Size(max = 100)
        String department,

        @NotBlank(message = "Position is required")
        @Size(max = 100)
        String position,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotNull(message = "Joining date is required")
        LocalDate joiningDate,

        @Size(max = 255)
        String address
) {}
