package com.example.auth;

public record JwtPayload(
        Long accountId,
        Long userId,
        String username,
        String role,
        String jti,
        Long exp
) {
}
