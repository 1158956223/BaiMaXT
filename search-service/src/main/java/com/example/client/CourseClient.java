package com.example.client;

import com.example.api.ApiResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/courses/admin")
    ApiResponse<List<CourseListClientResponse>> listAll(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                                        @RequestParam(name = "keyword", required = false) String keyword);

    @GetMapping("/api/courses/admin/{id}")
    ApiResponse<CourseDetailClientResponse> getAdminDetail(@PathVariable("id") Long id);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CourseListClientResponse(
            Long id,
            Long categoryId,
            String categoryName,
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
            TeacherSummaryClientResponse teacher
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CourseDetailClientResponse(
            Long id,
            Long categoryId,
            String categoryName,
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
            String targetAudience,
            String intro,
            String outline,
            String status,
            Integer sortOrder,
            TeacherClientResponse teacher,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TeacherSummaryClientResponse(
            Long id,
            String name,
            String title
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TeacherClientResponse(
            Long id,
            String name
    ) {
    }
}
