package com.example.domain.dto;

import com.example.domain.enums.PayType;

public record CreatePaymentRequest(
        String orderNo,
        Long userId,
        PayType payType
) {
}
