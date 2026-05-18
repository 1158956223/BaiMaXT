package com.example.domain.vo;

import com.example.domain.enums.PayFlowType;
import com.example.domain.enums.PayType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentFlowResponse(
        Long id,
        String payNo,
        String orderNo,
        Long userId,
        PayFlowType flowType,
        PayType payType,
        BigDecimal amount,
        String tradeNo,
        String remark,
        String rawContent,
        LocalDateTime createdAt
) {
}
