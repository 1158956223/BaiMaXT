package com.example.domain.vo;

import com.example.domain.dto.UserProfileResponse;

public record CurrentUserResponse(
        Long accountId,
        Long userId,
        String username,
        String role,
        UserProfileResponse user
) {
}
