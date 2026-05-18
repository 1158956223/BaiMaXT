package com.example.domain.vo;

import com.example.domain.enums.OrderStatus;
import com.example.domain.enums.PayStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPaymentConfirmResponse(
        String orderNo,
        Long userId,
        BigDecimal payAmount,
        PayStatus payStatus,
        OrderStatus orderStatus,
        LocalDateTime payTime
) {
}
