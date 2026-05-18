package com.example.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPayableResponse(
        String orderNo,
        Long userId,
        BigDecimal payAmount,
        String subject,
        String description,
        LocalDateTime expireTime
) {
}
