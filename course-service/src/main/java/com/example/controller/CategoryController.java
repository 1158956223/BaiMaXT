package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.dto.CategoryRequest;
import com.example.domain.vo.CategoryResponse;
import com.example.service.CategoryService;
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
@RequestMapping("/api/course-categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> listEnabled() {
        return ApiResponse.success(categoryService.listEnabled());
    }

    @GetMapping("/admin")
    public ApiResponse<List<CategoryResponse>> listAll() {
        return ApiResponse.success(categoryService.listAll());
    }

    @PostMapping("/admin")
    public ApiResponse<CategoryResponse> create(@RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.create(request));
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    @PatchMapping("/admin/{id}/enable")
    public ApiResponse<CategoryResponse> enable(@PathVariable Long id) {
        return ApiResponse.success(categoryService.enable(id));
    }

    @PatchMapping("/admin/{id}/disable")
    public ApiResponse<CategoryResponse> disable(@PathVariable Long id) {
        return ApiResponse.success(categoryService.disable(id));
    }
}
