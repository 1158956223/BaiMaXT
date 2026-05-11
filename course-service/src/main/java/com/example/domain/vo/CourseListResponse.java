package com.example.domain.vo;

import com.example.domain.enums.CourseStatus;
import com.example.domain.enums.CourseType;
import java.math.BigDecimal;

public record CourseListResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String title,
        String subtitle,
        String coverUrl,
        BigDecimal price,
        BigDecimal originalPrice,
        CourseType courseType,
        String durationDesc,
        CourseStatus status,
        Integer sortOrder,
        TeacherSummaryResponse teacher
) {
}
