package com.example.service;

import com.example.auth.JwtTool;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService implements UserIdResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTool jwtTool;

    public JwtTokenService(JwtTool jwtTool) {
        this.jwtTool = jwtTool;
    }

    @Override
    public Long parseUserId(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Invalid or missing token");
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Invalid or missing token");
        }
        return jwtTool.validate(token).userId();
    }
}
