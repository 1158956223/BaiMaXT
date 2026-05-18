package com.example.mq;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSuccessMessage(
        String orderNo,
        String payNo,
        String payType,
        BigDecimal paidAmount,
        LocalDateTime paidTime
) {
}
