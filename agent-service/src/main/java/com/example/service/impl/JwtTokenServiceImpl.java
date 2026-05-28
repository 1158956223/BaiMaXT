package com.example.service.impl;

import com.example.auth.JwtTool;
import com.example.service.JwtTokenService;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTool jwtTool;

    public JwtTokenServiceImpl(JwtTool jwtTool) {
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
