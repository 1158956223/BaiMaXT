package com.example.domain.vo;

import java.util.List;

public record SearchPageResponse<T>(
        List<T> records,
        long total,
        int page,
        int size,
        int totalPages
) {
}
