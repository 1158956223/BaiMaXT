package com.example.client;

import com.example.api.ApiResponse;
import com.example.domain.enums.OrderCourseStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/courses/admin/{id}")
    ApiResponse<CourseDetailClientResponse> getAdminDetail(@PathVariable Long id);

    @GetMapping("/api/courses/teacher")
    ApiResponse<List<CourseListClientResponse>> listTeacherCourses(@RequestHeader("X-User-Id") Long userId,
                                                                    @RequestHeader("X-User-Role") String role);

    @GetMapping("/api/teachers/internal/by-user/{userId}")
    ApiResponse<TeacherClientResponse> getTeacherByUserId(@PathVariable Long userId);

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
    record CourseListClientResponse(
            Long id,
            String title,
            String subtitle,
            BigDecimal price,
            Integer stock,
            Integer soldCount,
            Integer availableStock,
            String status,
            TeacherClientResponse teacher,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TeacherClientResponse(
            Long id,
            Long userId,
            String name
    ) {
    }
}
