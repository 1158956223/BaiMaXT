package com.example.service;

import com.example.domain.dto.LoginRequest;
import com.example.domain.dto.RegisterRequest;
import com.example.domain.vo.AuthResponse;
import com.example.domain.vo.CurrentUserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    CurrentUserResponse me(String authorizationHeader);

    void logout(String authorizationHeader);
}
