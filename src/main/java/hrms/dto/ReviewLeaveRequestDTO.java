package hrms.dto;


import jakarta.validation.constraints.Size;

public record ReviewLeaveRequestDTO(

        @Size(max = 500)
        String comment
) {}

