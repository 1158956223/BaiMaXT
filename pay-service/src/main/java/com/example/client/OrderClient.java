package com.example.client;

import com.example.api.ApiResponse;
import com.example.domain.enums.PayType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/internal/{orderNo}/payable")
    ApiResponse<OrderPayableResponse> getPayable(@PathVariable String orderNo, @RequestParam Long userId);

    @PostMapping("/api/orders/internal/{orderNo}/pay-confirm")
    ApiResponse<OrderPaymentConfirmResponse> confirmPayment(@PathVariable String orderNo,
                                                            @RequestBody ConfirmPaymentRequest request);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OrderPayableResponse(
            String orderNo,
            Long userId,
            BigDecimal payAmount,
            String subject,
            String description,
            LocalDateTime expireTime
    ) {
    }

    record ConfirmPaymentRequest(
            String payNo,
            PayType payType,
            BigDecimal paidAmount,
            LocalDateTime paidTime
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OrderPaymentConfirmResponse(
            String orderNo,
            Long userId,
            BigDecimal payAmount,
            String payStatus,
            String orderStatus,
            LocalDateTime payTime
    ) {
    }
}
