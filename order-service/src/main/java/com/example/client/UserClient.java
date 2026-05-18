package com.example.client;

import com.example.api.ApiResponse;
import com.example.dto.user.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/internal/{id}")
    ApiResponse<UserProfileResponse> getInternal(@PathVariable Long id);
}
