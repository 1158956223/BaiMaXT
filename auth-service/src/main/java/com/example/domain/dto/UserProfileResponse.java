package com.example.domain.dto;

import com.example.enums.AccountRole;
import com.example.enums.AccountStatus;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String username,
        String nickname,
        String phone,
        String email,
        AccountRole role,
        AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
