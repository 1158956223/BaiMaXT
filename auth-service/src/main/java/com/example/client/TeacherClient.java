package com.example.client;

import com.example.api.ApiResponse;
import com.example.dto.teacher.CreateTeacherProfileRequest;
import com.example.dto.teacher.TeacherProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "course-service")
public interface TeacherClient {

    @PostMapping("/api/teachers/internal")
    ApiResponse<TeacherProfileResponse> createInternal(@RequestBody CreateTeacherProfileRequest request);
}
