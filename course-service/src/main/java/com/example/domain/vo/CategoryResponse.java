package com.example.domain.vo;

import com.example.domain.enums.EnabledStatus;
import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        Long parentId,
        String name,
        Integer sortOrder,
        EnabledStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
