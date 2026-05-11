package com.example.domain.dto;

public record TeacherRequest(
        String name,
        String avatarUrl,
        String title,
        String bio,
        String specialties,
        Integer yearsExperience
) {
}
