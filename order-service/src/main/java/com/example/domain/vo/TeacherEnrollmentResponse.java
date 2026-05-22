package com.example.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TeacherEnrollmentResponse(
        Long orderId,
        String orderNo,
        Long studentUserId,
        String studentName,
        Long courseId,
        String courseTitle,
        String courseSubtitle,
        BigDecimal payAmount,
        LocalDateTime payTime,
        LocalDateTime createdAt
) {
}
