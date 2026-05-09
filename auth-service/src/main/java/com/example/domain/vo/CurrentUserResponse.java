package com.example.domain.vo;

import com.example.domain.dto.UserProfileResponse;
import com.example.enums.UserRole;

public record CurrentUserResponse(
        Long accountId,
        Long userId,
        String username,
        UserRole role,
        UserProfileResponse user
) {
}
