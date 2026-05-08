package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.dto.LoginRequest;
import com.example.domain.dto.RegisterRequest;
import com.example.domain.vo.AuthResponse;
import com.example.domain.vo.CurrentUserResponse;
import com.example.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return ApiResponse.success(authService.me(authorizationHeader));
    }
}
