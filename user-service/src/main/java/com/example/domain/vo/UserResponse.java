package com.example.domain.vo;

import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String nickname,
        String phone,
        String email,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
