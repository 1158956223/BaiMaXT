package com.example.domain.vo;

import com.example.domain.enums.CourseStatus;
import com.example.domain.enums.CourseType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourseDetailResponse(
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
        Integer stock,
        Integer soldCount,
        Integer availableStock,
        String targetAudience,
        String intro,
        String outline,
        CourseStatus status,
        Integer sortOrder,
        TeacherResponse teacher,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
