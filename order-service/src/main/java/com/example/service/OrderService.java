package com.example.service;

import com.example.domain.dto.CreateOrderRequest;
import com.example.domain.enums.OrderStatus;
import com.example.domain.enums.PayStatus;
import com.example.domain.vo.OrderDetailResponse;
import com.example.domain.vo.OrderResponse;
import java.util.List;

public interface OrderService {

    OrderResponse create(CreateOrderRequest request);

    List<OrderResponse> listMine(Long userId);

    OrderDetailResponse getDetail(Long id, Long userId);

    OrderResponse cancel(Long id, Long userId);

    OrderResponse mockPay(Long id, Long userId);

    List<OrderResponse> listAdmin(Long userId, OrderStatus orderStatus, PayStatus payStatus);
}
