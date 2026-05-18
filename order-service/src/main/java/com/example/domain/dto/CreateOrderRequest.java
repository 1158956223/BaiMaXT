package com.example.domain.dto;

public record CreateOrderRequest(
        Long userId,
        Long courseId,
        String remark
) {
}
