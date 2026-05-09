package com.example.client;

import com.example.api.ApiResponse;
import com.example.dto.user.CreateUserProfileRequest;
import com.example.dto.user.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("/api/users/internal")
    ApiResponse<UserProfileResponse> createInternal(@RequestBody CreateUserProfileRequest request);

    @GetMapping("/api/users/internal/{id}")
    ApiResponse<UserProfileResponse> getInternal(@PathVariable Long id);
}
