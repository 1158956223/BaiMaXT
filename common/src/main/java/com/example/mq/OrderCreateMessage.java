package com.example.mq;

public record OrderCreateMessage(
        String requestId,
        Long userId,
        Long courseId,
        String remark
) {
}
