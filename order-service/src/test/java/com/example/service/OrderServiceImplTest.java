package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.api.ApiResponse;
import com.example.client.CourseClient;
import com.example.client.CourseClient.CourseDetailClientResponse;
import com.example.client.UserClient;
import com.example.domain.dto.CreateOrderRequest;
import com.example.domain.enums.OrderCourseStatus;
import com.example.domain.enums.OrderSubmitStatus;
import com.example.domain.vo.OrderSubmitResponse;
import com.example.dto.user.UserProfileResponse;
import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import com.example.mapper.OrderMapper;
import com.example.mapper.OrderStatusLogMapper;
import com.example.mq.MqConstants;
import com.example.mq.OrderCreateMessage;
import com.example.service.impl.OrderServiceImpl;
import com.example.support.RedisCourseStockGuard;
import com.example.support.RedisSubmitLock;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class OrderServiceImplTest {

    private final OrderMapper orderMapper = org.mockito.Mockito.mock(OrderMapper.class);
    private final OrderStatusLogMapper statusLogMapper = org.mockito.Mockito.mock(OrderStatusLogMapper.class);
    private final UserClient userClient = org.mockito.Mockito.mock(UserClient.class);
    private final CourseClient courseClient = org.mockito.Mockito.mock(CourseClient.class);
    private final RabbitTemplate rabbitTemplate = org.mockito.Mockito.mock(RabbitTemplate.class);
    private final RedisSubmitLock redisSubmitLock = org.mockito.Mockito.mock(RedisSubmitLock.class);
    private final RedisCourseStockGuard stockGuard = org.mockito.Mockito.mock(RedisCourseStockGuard.class);

    private final OrderServiceImpl orderService = new OrderServiceImpl(
            orderMapper,
            statusLogMapper,
            userClient,
            courseClient,
            rabbitTemplate,
            redisSubmitLock,
            stockGuard
    );

    @Test
    void createReservesRedisStockAndQueuesOrderCreation() {
        when(userClient.getInternal(7L)).thenReturn(ApiResponse.success(new UserProfileResponse(
                7L,
                "student",
                "Student",
                "13800000000",
                "student@example.com",
                UserRole.STUDENT,
                UserStatus.ENABLED,
                null,
                null
        )));
        when(courseClient.getAdminDetail(11L)).thenReturn(ApiResponse.success(new CourseDetailClientResponse(
                11L,
                "Java",
                "Concurrency",
                new BigDecimal("99.00"),
                new BigDecimal("199.00"),
                10,
                2,
                8,
                OrderCourseStatus.ON_SALE,
                null,
                null,
                null
        )));
        when(redisSubmitLock.tryLock(eq("baimaxt:order:submit:7:11"), any())).thenReturn("lock-token");
        when(stockGuard.reserve(eq(11L), eq(7L), any(), eq(8))).thenReturn(OrderSubmitStatus.QUEUEING);

        OrderSubmitResponse response = orderService.create(new CreateOrderRequest(7L, 11L, ""));

        assertEquals(OrderSubmitStatus.QUEUEING, response.status());
        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.ORDER_EXCHANGE),
                eq(MqConstants.ORDER_CREATE_ROUTING_KEY),
                any(OrderCreateMessage.class)
        );
        verify(courseClient, never()).decreaseStock(11L);
        verify(orderMapper, never()).insert(any());
    }

    @Test
    void getSubmitResultReadsRedisResultByRequestId() {
        when(stockGuard.getResult("request-1")).thenReturn(new OrderSubmitResponse(
                "request-1",
                OrderSubmitStatus.SUCCESS,
                99L,
                "BM202605220001",
                "Order created"
        ));

        OrderSubmitResponse response = orderService.getSubmitResult("request-1");

        assertEquals(OrderSubmitStatus.SUCCESS, response.status());
        assertEquals(99L, response.orderId());
    }
}
