package com.example.service;

import com.example.domain.dto.CreateOrderRequest;
import com.example.domain.dto.ConfirmPaymentRequest;
import com.example.domain.enums.OrderStatus;
import com.example.domain.enums.PayStatus;
import com.example.domain.vo.OrderDetailResponse;
import com.example.domain.vo.OrderPayableResponse;
import com.example.domain.vo.OrderPaymentConfirmResponse;
import com.example.domain.vo.OrderResponse;
import com.example.domain.vo.StudentCourseResponse;
import com.example.domain.vo.TeacherDashboardStatsResponse;
import com.example.domain.vo.TeacherEnrollmentResponse;
import com.example.mq.OrderTimeoutMessage;
import com.example.mq.PaymentSuccessMessage;
import java.util.List;

public interface OrderService {

    OrderResponse create(CreateOrderRequest request);

    List<OrderResponse> listMine(Long userId);

    List<StudentCourseResponse> listMyCourses(Long userId);

    List<TeacherEnrollmentResponse> listTeacherEnrollments(Long userId, Long courseId);

    TeacherDashboardStatsResponse getTeacherDashboardStats(Long userId);

    OrderDetailResponse getDetail(Long id, Long userId);

    OrderResponse cancel(Long id, Long userId);

    OrderResponse mockPay(Long id, Long userId);

    List<OrderResponse> listAdmin(Long userId, OrderStatus orderStatus, PayStatus payStatus);

    OrderPayableResponse getPayable(String orderNo, Long userId);

    OrderPaymentConfirmResponse confirmPayment(String orderNo, ConfirmPaymentRequest request);

    void closeExpiredOrder(OrderTimeoutMessage message);

    void handlePaymentSuccess(PaymentSuccessMessage message);
}
