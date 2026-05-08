package com.example.domain.dto;

public record UpdateUserRequest(
        String nickname,
        String phone,
        String email
) {
}
