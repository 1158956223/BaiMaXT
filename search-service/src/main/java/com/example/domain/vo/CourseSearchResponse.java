package com.example.domain.vo;

import java.math.BigDecimal;

public record CourseSearchResponse(
        Long id,
        Long categoryId,
        String categoryName,
        Long teacherId,
        String teacherName,
        String title,
        String subtitle,
        String coverUrl,
        BigDecimal price,
        BigDecimal originalPrice,
        String courseType,
        String durationDesc,
        Integer stock,
        Integer soldCount,
        Integer availableStock,
        String status,
        Integer sortOrder,
        Float score
) {
}
