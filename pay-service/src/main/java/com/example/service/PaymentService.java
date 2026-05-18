package com.example.service;

import com.example.domain.dto.CreatePaymentRequest;
import com.example.domain.enums.PayStatus;
import com.example.domain.vo.PaymentDetailResponse;
import com.example.domain.vo.PaymentResponse;
import java.util.List;

public interface PaymentService {

    PaymentResponse create(CreatePaymentRequest request);

    PaymentDetailResponse getDetail(String payNo, Long userId);

    PaymentResponse mockSuccess(String payNo, Long userId);

    PaymentResponse mockFail(String payNo, Long userId);

    List<PaymentResponse> listMine(Long userId);

    List<PaymentResponse> listAdmin(Long userId, PayStatus payStatus);
}
