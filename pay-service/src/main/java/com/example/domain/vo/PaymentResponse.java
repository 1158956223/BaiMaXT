package com.example.domain.vo;

import com.example.domain.enums.PayStatus;
import com.example.domain.enums.PayType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String payNo,
        String orderNo,
        Long userId,
        BigDecimal payAmount,
        PayType payType,
        PayStatus payStatus,
        String subject,
        String description,
        String tradeNo,
        LocalDateTime expireTime,
        LocalDateTime payTime,
        LocalDateTime closeTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
