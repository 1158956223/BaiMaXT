package com.example.domain.vo;

import com.example.domain.dto.UserProfileResponse;
import com.example.enums.AccountRole;

public record AuthResponse(
        String token,
        Long accountId,
        Long userId,
        String username,
        AccountRole role,
        UserProfileResponse user
) {
}
