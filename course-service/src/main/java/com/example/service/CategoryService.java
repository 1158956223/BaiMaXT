package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.dto.CategoryRequest;
import com.example.domain.vo.CategoryResponse;
import com.example.domain.po.CourseCategory;
import java.util.List;

public interface CategoryService extends IService<CourseCategory> {

    List<CategoryResponse> listEnabled();

    List<CategoryResponse> listAll();

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    CategoryResponse enable(Long id);

    CategoryResponse disable(Long id);
}
