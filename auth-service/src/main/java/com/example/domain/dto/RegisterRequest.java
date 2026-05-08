package com.example.domain.dto;

public record RegisterRequest(
        String username,
        String password,
        String nickname,
        String phone,
        String email,
        String role
) {
}
