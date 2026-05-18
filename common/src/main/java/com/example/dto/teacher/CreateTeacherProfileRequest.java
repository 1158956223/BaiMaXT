package com.example.dto.teacher;

public record CreateTeacherProfileRequest(
        Long userId,
        String name,
        String title,
        String bio,
        String specialties,
        Integer yearsExperience
) {
}
