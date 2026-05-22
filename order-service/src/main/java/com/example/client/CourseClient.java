package com.example.client;

import com.example.api.ApiResponse;
import com.example.domain.enums.OrderCourseStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/courses/admin/{id}")
    ApiResponse<CourseDetailClientResponse> getAdminDetail(@PathVariable Long id);

    @PostMapping("/api/courses/internal/{id}/stock/decrease")
    ApiResponse<Void> decreaseStock(@PathVariable Long id);

    @PostMapping("/api/courses/internal/{id}/stock/restore")
    ApiResponse<Void> restoreStock(@PathVariable Long id);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CourseDetailClientResponse(
            Long id,
            String title,
            String subtitle,
            BigDecimal price,
            BigDecimal originalPrice,
            Integer stock,
            Integer soldCount,
            Integer availableStock,
            OrderCourseStatus status,
            TeacherClientResponse teacher,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TeacherClientResponse(
            Long id,
            String name
    ) {
    }
}
