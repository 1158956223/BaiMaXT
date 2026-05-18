package com.example.domain.vo;

import com.example.domain.enums.OrderOperateType;
import com.example.domain.enums.OrderStatus;
import com.example.domain.enums.PayStatus;
import com.example.enums.UserRole;
import java.time.LocalDateTime;

public record OrderStatusLogResponse(
        Long id,
        Long orderId,
        String orderNo,
        OrderStatus oldOrderStatus,
        OrderStatus newOrderStatus,
        PayStatus oldPayStatus,
        PayStatus newPayStatus,
        OrderOperateType operateType,
        Long operateBy,
        UserRole operateRole,
        String remark,
        LocalDateTime createdAt
) {
}
