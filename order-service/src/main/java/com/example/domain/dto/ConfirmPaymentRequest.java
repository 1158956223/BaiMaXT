package com.example.domain.dto;

import com.example.domain.enums.PayType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConfirmPaymentRequest(
        String payNo,
        PayType payType,
        BigDecimal paidAmount,
        LocalDateTime paidTime
) {
}
