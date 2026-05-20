package com.example.mq;

import java.time.LocalDateTime;

public record CourseIndexMessage(
        Long courseId,
        String eventType,
        LocalDateTime occurredAt
) {
}
