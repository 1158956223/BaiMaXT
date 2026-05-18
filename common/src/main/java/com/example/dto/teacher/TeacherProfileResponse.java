package com.example.dto.teacher;

import java.time.LocalDateTime;

public record TeacherProfileResponse(
        Long id,
        Long userId,
        String name,
        String title,
        String bio,
        String specialties,
        Integer yearsExperience,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
