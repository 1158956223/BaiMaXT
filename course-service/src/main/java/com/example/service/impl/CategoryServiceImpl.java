package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.dto.CategoryRequest;
import com.example.domain.vo.CategoryResponse;
import com.example.domain.enums.EnabledStatus;
import com.example.domain.po.CourseCategory;
import com.example.exception.BusinessException;
import com.example.mapper.CourseCategoryMapper;
import com.example.service.CategoryService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CategoryService {

    private final CourseCategoryMapper categoryMapper;

    public CategoryServiceImpl(CourseCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryResponse> listEnabled() {
        return categoryMapper.selectList(baseQuery().eq(CourseCategory::getStatus, EnabledStatus.ENABLED)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> listAll() {
        return categoryMapper.selectList(baseQuery()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        LocalDateTime now = LocalDateTime.now();
        CourseCategory category = new CourseCategory();
        category.setParentId(request.parentId() == null ? 0L : request.parentId());
        category.setName(requireText(request.name(), "Category name must not be blank"));
        category.setSortOrder(defaultSort(request.sortOrder()));
        category.setStatus(EnabledStatus.ENABLED);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        categoryMapper.insert(category);
        return toResponse(category);
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        CourseCategory category = getCategory(id);
        category.setParentId(request.parentId() == null ? 0L : request.parentId());
        category.setName(requireText(request.name(), "Category name must not be blank"));
        category.setSortOrder(defaultSort(request.sortOrder()));
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
        return toResponse(category);
    }

    @Override
    public CategoryResponse enable(Long id) {
        CourseCategory category = getCategory(id);
        category.setStatus(EnabledStatus.ENABLED);
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
        return toResponse(category);
    }

    @Override
    public CategoryResponse disable(Long id) {
        CourseCategory category = getCategory(id);
        category.setStatus(EnabledStatus.DISABLED);
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
        return toResponse(category);
    }

    private CourseCategory getCategory(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Category id must not be null");
        }
        CourseCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Category does not exist");
        }
        return category;
    }

    private LambdaQueryWrapper<CourseCategory> baseQuery() {
        return new LambdaQueryWrapper<CourseCategory>()
                .orderByAsc(CourseCategory::getSortOrder)
                .orderByDesc(CourseCategory::getId);
    }

    private CategoryResponse toResponse(CourseCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getParentId(),
                category.getName(),
                category.getSortOrder(),
                category.getStatus(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private String requireText(String value, String message) {
        String text = trimToNull(value);
        if (text == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
        return text;
    }

    private Integer defaultSort(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
