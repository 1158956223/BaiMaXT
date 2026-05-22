package com.example.domain.vo;

import java.math.BigDecimal;
import java.util.List;

public record TeacherDashboardStatsResponse(
        Long teacherId,
        Integer totalCourses,
        Integer onSaleCourses,
        Long paidEnrollments,
        Long studentCount,
        Integer availableStock,
        BigDecimal revenue,
        List<TeacherCourseStatsResponse> courses
) {
}
