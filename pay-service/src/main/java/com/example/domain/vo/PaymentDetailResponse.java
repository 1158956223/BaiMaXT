package com.example.domain.vo;

import java.util.List;

public record PaymentDetailResponse(
        PaymentResponse payment,
        List<PaymentFlowResponse> flows
) {
}
