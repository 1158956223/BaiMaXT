package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.vo.CourseDetailResponse;
import com.example.domain.vo.CourseListResponse;
import com.example.domain.dto.CourseRequest;
import com.example.service.CourseService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<List<CourseListResponse>> listOnSale(@RequestParam(required = false) Long categoryId,
                                                            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(courseService.listOnSale(categoryId, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseDetailResponse> getPublicDetail(@PathVariable Long id) {
        return ApiResponse.success(courseService.getPublicDetail(id));
    }

    @GetMapping("/admin")
    public ApiResponse<List<CourseListResponse>> listAll(@RequestParam(required = false) Long categoryId,
                                                         @RequestParam(required = false) String keyword) {
        return ApiResponse.success(courseService.listAll(categoryId, keyword));
    }

    @GetMapping("/admin/{id}")
    public ApiResponse<CourseDetailResponse> getAdminDetail(@PathVariable Long id) {
        return ApiResponse.success(courseService.getAdminDetail(id));
    }

    @PostMapping("/admin")
    public ApiResponse<CourseDetailResponse> create(@RequestBody CourseRequest request) {
        return ApiResponse.success(courseService.create(request));
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<CourseDetailResponse> update(@PathVariable Long id, @RequestBody CourseRequest request) {
        return ApiResponse.success(courseService.update(id, request));
    }

    @PatchMapping("/admin/{id}/on-sale")
    public ApiResponse<CourseDetailResponse> onSale(@PathVariable Long id) {
        return ApiResponse.success(courseService.onSale(id));
    }

    @PatchMapping("/admin/{id}/off-sale")
    public ApiResponse<CourseDetailResponse> offSale(@PathVariable Long id) {
        return ApiResponse.success(courseService.offSale(id));
    }
}
