package com.example.domain.dto;

public record CategoryRequest(
        Long parentId,
        String name,
        Integer sortOrder
) {
}
