package com.example.domain.vo;

import com.example.domain.enums.OrderSubmitStatus;

public record OrderSubmitResponse(
        String requestId,
        OrderSubmitStatus status,
        Long orderId,
        String orderNo,
        String message
) {
}
