package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.dto.CreateUserRequest;
import com.example.domain.dto.LoginRequest;
import com.example.domain.dto.UpdateUserRequest;
import com.example.domain.dto.UserResponse;
import com.example.service.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<UserResponse> create(@RequestBody CreateUserRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @PostMapping("/login")
    public ApiResponse<UserResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> list() {
        return ApiResponse.success(userService.listUser());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable Long id) {
        return ApiResponse.success(userService.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }

    @PatchMapping("/{id}/disable")
    public ApiResponse<UserResponse> disable(@PathVariable Long id) {
        return ApiResponse.success(userService.disable(id));
    }

    @PatchMapping("/{id}/enable")
    public ApiResponse<UserResponse> enable(@PathVariable Long id) {
        return ApiResponse.success(userService.enable(id));
    }
}
