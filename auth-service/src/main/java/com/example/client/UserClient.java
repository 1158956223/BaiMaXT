package com.example.client;

import com.example.api.ApiResponse;
import com.example.domain.dto.CreateUserProfileRequest;
import com.example.domain.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("/api/users/internal")
    ApiResponse<UserProfileResponse> createUser(@RequestBody CreateUserProfileRequest request);

    @GetMapping("/api/users/{id}")
    ApiResponse<UserProfileResponse> getUser(@PathVariable Long id);
}
