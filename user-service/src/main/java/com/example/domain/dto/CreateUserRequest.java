package com.example.domain.dto;

import com.example.enums.UserRole;

public record CreateUserRequest(
        String username,
        String password,
        String nickname,
        String phone,
        String email,
        UserRole role
) {
}
