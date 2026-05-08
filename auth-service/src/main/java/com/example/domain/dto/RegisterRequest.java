package com.example.domain.dto;

import com.example.enums.AccountRole;

public record RegisterRequest(
        String username,
        String password,
        String nickname,
        String phone,
        String email,
        AccountRole role
) {
}
