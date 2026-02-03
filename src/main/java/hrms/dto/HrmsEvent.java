package hrms.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record HrmsEvent(
        String eventType,
        Map<String, Object> eventData,
        String description,
        LocalDateTime timestamp
) {
    public HrmsEvent(String eventType, Map<String, Object> eventData, String description) {
        this(eventType, eventData, description, LocalDateTime.now());
    }
}

