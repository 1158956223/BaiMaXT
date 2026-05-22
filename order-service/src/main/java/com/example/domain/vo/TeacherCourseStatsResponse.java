package com.example.domain.vo;

import java.math.BigDecimal;

public record TeacherCourseStatsResponse(
        Long courseId,
        String courseTitle,
        String courseSubtitle,
        Integer stock,
        Integer soldCount,
        Integer availableStock,
        Long paidEnrollments,
        BigDecimal revenue
) {
}
