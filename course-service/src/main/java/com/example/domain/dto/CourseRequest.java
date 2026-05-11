package com.example.domain.dto;

import com.example.domain.enums.CourseStatus;
import com.example.domain.enums.CourseType;
import java.math.BigDecimal;

public record CourseRequest(
        Long categoryId,
        Long teacherId,
        String title,
        String subtitle,
        String coverUrl,
        BigDecimal price,
        BigDecimal originalPrice,
        CourseType courseType,
        String durationDesc,
        String targetAudience,
        String intro,
        String outline,
        CourseStatus status,
        Integer sortOrder
) {
}
