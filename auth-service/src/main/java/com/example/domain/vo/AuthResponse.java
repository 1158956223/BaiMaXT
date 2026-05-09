package com.example.domain.vo;

import com.example.dto.user.UserProfileResponse;
import com.example.enums.UserRole;

public record AuthResponse(
        String token,
        Long accountId,
        Long userId,
        String username,
        UserRole role,
        UserProfileResponse user
) {
}
