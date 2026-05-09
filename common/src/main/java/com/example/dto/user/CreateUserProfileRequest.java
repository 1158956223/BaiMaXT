package com.example.dto.user;

import com.example.enums.UserRole;

public record CreateUserProfileRequest(
        String username,
        String nickname,
        String phone,
        String email,
        UserRole role
) {
}
