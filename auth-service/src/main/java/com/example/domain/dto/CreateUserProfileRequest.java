package com.example.domain.dto;

public record CreateUserProfileRequest(
        String username,
        String nickname,
        String phone,
        String email,
        String role
) {
}
