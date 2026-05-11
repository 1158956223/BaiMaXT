package com.example.domain.vo;

import com.example.domain.enums.EnabledStatus;
import java.time.LocalDateTime;

public record TeacherResponse(
        Long id,
        String name,
        String avatarUrl,
        String title,
        String bio,
        String specialties,
        Integer yearsExperience,
        EnabledStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
