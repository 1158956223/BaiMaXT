package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.dto.CreatePaymentRequest;
import com.example.domain.enums.PayStatus;
import com.example.domain.vo.PaymentDetailResponse;
import com.example.domain.vo.PaymentResponse;
import com.example.service.PaymentService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ApiResponse<PaymentResponse> create(@RequestBody CreatePaymentRequest request) {
        return ApiResponse.success(paymentService.create(request));
    }

    @GetMapping("/my")
    public ApiResponse<List<PaymentResponse>> listMine(@RequestParam Long userId) {
        return ApiResponse.success(paymentService.listMine(userId));
    }

    @GetMapping("/{payNo}")
    public ApiResponse<PaymentDetailResponse> detail(@PathVariable String payNo,
                                                     @RequestParam(required = false) Long userId) {
        return ApiResponse.success(paymentService.getDetail(payNo, userId));
    }

    @PostMapping("/{payNo}/mock-success")
    public ApiResponse<PaymentResponse> mockSuccess(@PathVariable String payNo, @RequestParam Long userId) {
        return ApiResponse.success(paymentService.mockSuccess(payNo, userId));
    }

    @PostMapping("/{payNo}/mock-fail")
    public ApiResponse<PaymentResponse> mockFail(@PathVariable String payNo, @RequestParam Long userId) {
        return ApiResponse.success(paymentService.mockFail(payNo, userId));
    }

    @GetMapping("/admin")
    public ApiResponse<List<PaymentResponse>> listAdmin(@RequestParam(required = false) Long userId,
                                                        @RequestParam(required = false) PayStatus payStatus) {
        return ApiResponse.success(paymentService.listAdmin(userId, payStatus));
    }
}
