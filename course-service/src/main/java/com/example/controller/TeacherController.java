package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.dto.TeacherRequest;
import com.example.domain.vo.TeacherResponse;
import com.example.dto.teacher.CreateTeacherProfileRequest;
import com.example.dto.teacher.TeacherProfileResponse;
import com.example.service.TeacherService;
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
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ApiResponse<List<TeacherResponse>> listEnabled() {
        return ApiResponse.success(teacherService.listEnabled());
    }

    @GetMapping("/admin")
    public ApiResponse<List<TeacherResponse>> listAll() {
        return ApiResponse.success(teacherService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<TeacherResponse> get(@PathVariable Long id) {
        return ApiResponse.success(teacherService.getDetail(id));
    }

    @PostMapping("/admin")
    public ApiResponse<TeacherResponse> create(@RequestBody TeacherRequest request) {
        return ApiResponse.success(teacherService.create(request));
    }

    @PostMapping("/internal")
    public ApiResponse<TeacherProfileResponse> createInternal(@RequestBody CreateTeacherProfileRequest request) {
        return ApiResponse.success(teacherService.createInternal(request));
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<TeacherResponse> update(@PathVariable Long id, @RequestBody TeacherRequest request) {
        return ApiResponse.success(teacherService.update(id, request));
    }

    @PatchMapping("/admin/{id}/enable")
    public ApiResponse<TeacherResponse> enable(@PathVariable Long id) {
        return ApiResponse.success(teacherService.enable(id));
    }

    @PatchMapping("/admin/{id}/disable")
    public ApiResponse<TeacherResponse> disable(@PathVariable Long id) {
        return ApiResponse.success(teacherService.disable(id));
    }
}
