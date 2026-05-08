package com.example.domain.dto;

import com.example.enums.UserRole;

public record InternalCreateUserRequest(
        String username,
        String nickname,
        String phone,
        String email,
        UserRole role
) {
}
