package com.example.domain.vo;

import com.example.domain.dto.UserProfileResponse;

public record AuthResponse(
        String token,
        Long accountId,
        Long userId,
        String username,
        String role,
        UserProfileResponse user
) {
}
