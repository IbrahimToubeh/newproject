package hrms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveResponseDTO(
        Long id,
        Long reporterId,
        Long employeeId,
        String email,
        LocalDate startDate,
        LocalDate endDate,
        String leaveType,
        String reason,
        String status,
        Long approvedBy,
        LocalDateTime reviewedAt,
        String adminComment,
        LocalDateTime createdAt,
        Long leaveDays
) {}

