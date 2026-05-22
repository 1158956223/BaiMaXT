package com.example.controller;

import com.example.api.ApiResponse;
import com.example.domain.dto.ConfirmPaymentRequest;
import com.example.domain.dto.CreateOrderRequest;
import com.example.domain.enums.OrderStatus;
import com.example.domain.enums.PayStatus;
import com.example.domain.vo.OrderDetailResponse;
import com.example.domain.vo.OrderPayableResponse;
import com.example.domain.vo.OrderPaymentConfirmResponse;
import com.example.domain.vo.OrderResponse;
import com.example.domain.vo.StudentCourseResponse;
import com.example.domain.vo.TeacherDashboardStatsResponse;
import com.example.domain.vo.TeacherEnrollmentResponse;
import com.example.exception.BusinessException;
import com.example.service.OrderService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @GetMapping("/my-courses")
    public ApiResponse<List<StudentCourseResponse>> listMyCourses(@RequestParam Long userId) {
        return ApiResponse.success(orderService.listMyCourses(userId));
    }

    @GetMapping("/teacher/enrollments")
    public ApiResponse<List<TeacherEnrollmentResponse>> listTeacherEnrollments(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) Long courseId) {
        requireTeacher(role);
        return ApiResponse.success(orderService.listTeacherEnrollments(userId, courseId));
    }

    @GetMapping("/teacher/stats")
    public ApiResponse<TeacherDashboardStatsResponse> teacherStats(@RequestHeader("X-User-Id") Long userId,
                                                                   @RequestHeader("X-User-Role") String role) {
        requireTeacher(role);
        return ApiResponse.success(orderService.getTeacherDashboardStats(userId));
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

    @GetMapping("/internal/{orderNo}/payable")
    public ApiResponse<OrderPayableResponse> getPayable(@PathVariable String orderNo, @RequestParam Long userId) {
        return ApiResponse.success(orderService.getPayable(orderNo, userId));
    }

    @PostMapping("/internal/{orderNo}/pay-confirm")
    public ApiResponse<OrderPaymentConfirmResponse> confirmPayment(@PathVariable String orderNo,
                                                                   @RequestBody ConfirmPaymentRequest request) {
        return ApiResponse.success(orderService.confirmPayment(orderNo, request));
    }

    private void requireTeacher(String role) {
        if (!"TEACHER".equals(role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Teacher permission required");
        }
    }
}
