package hrms.dto;


import hrms.entity.LeaveType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateLeaveRequestDTO(

        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @Future(message = "End date must be in the future")
        LocalDate endDate,

        @NotNull(message = "Leave type is required")
        LeaveType leaveType,

        @NotBlank(message = "Reason is required")
        @Size(max = 1000)
        String reason
) {}

