package com.example.domain.vo;

import java.util.List;

public record OrderDetailResponse(
        OrderResponse order,
        List<OrderStatusLogResponse> statusLogs
) {
}
