package hrms.dto;


import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequestDTO(

        @Size(max = 20)
        String phoneNumber,

        @Size(max = 255)
        String address
) {}
