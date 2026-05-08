package com.example.domain.dto;

import com.example.enums.AccountRole;

public record CreateUserProfileRequest(
        String username,
        String nickname,
        String phone,
        String email,
        AccountRole role
) {
}
