package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.dto.CreateOrderRequest;
import com.example.domain.enums.OrderStatus;
import com.example.domain.enums.PayStatus;
import com.example.domain.vo.OrderDetailResponse;
import com.example.domain.vo.OrderResponse;
import com.example.service.OrderService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderResponse> create(@RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.create(request));
    }

    @GetMapping("/my")
    public ApiResponse<List<OrderResponse>> listMine(@RequestParam Long userId) {
        return ApiResponse.success(orderService.listMine(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> detail(@PathVariable Long id,
                                                   @RequestParam(required = false) Long userId) {
        return ApiResponse.success(orderService.getDetail(id, userId));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable Long id, @RequestParam Long userId) {
        return ApiResponse.success(orderService.cancel(id, userId));
    }

    @PatchMapping("/{id}/mock-pay")
    public ApiResponse<OrderResponse> mockPay(@PathVariable Long id, @RequestParam Long userId) {
        return ApiResponse.success(orderService.mockPay(id, userId));
    }

    @GetMapping("/admin")
    public ApiResponse<List<OrderResponse>> listAdmin(@RequestParam(required = false) Long userId,
                                                      @RequestParam(required = false) OrderStatus orderStatus,
                                                      @RequestParam(required = false) PayStatus payStatus) {
        return ApiResponse.success(orderService.listAdmin(userId, orderStatus, payStatus));
    }
}
