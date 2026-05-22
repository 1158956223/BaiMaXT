package com.example.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StudentCourseResponse(
        Long orderId,
        String orderNo,
        Long courseId,
        String courseTitle,
        String courseSubtitle,
        Long teacherId,
        String teacherName,
        BigDecimal payAmount,
        LocalDateTime payTime,
        LocalDateTime createdAt
) {
}
