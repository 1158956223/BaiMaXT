package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.api.ApiResponse;
import com.example.client.CourseClient;
import com.example.client.CourseClient.CourseDetailClientResponse;
import com.example.client.CourseClient.CourseListClientResponse;
import com.example.client.CourseClient.TeacherClientResponse;
import com.example.client.UserClient;
import com.example.domain.dto.ConfirmPaymentRequest;
import com.example.domain.dto.CreateOrderRequest;
import com.example.domain.enums.OrderCourseStatus;
import com.example.domain.enums.OrderOperateType;
import com.example.domain.enums.OrderStatus;
import com.example.domain.enums.OrderSubmitStatus;
import com.example.domain.enums.PayStatus;
import com.example.domain.enums.PayType;
import com.example.domain.po.Order;
import com.example.domain.po.OrderStatusLog;
import com.example.domain.vo.OrderDetailResponse;
import com.example.domain.vo.OrderPayableResponse;
import com.example.domain.vo.OrderPaymentConfirmResponse;
import com.example.domain.vo.OrderResponse;
import com.example.domain.vo.OrderSubmitResponse;
import com.example.domain.vo.OrderStatusLogResponse;
import com.example.domain.vo.StudentCourseResponse;
import com.example.domain.vo.TeacherCourseStatsResponse;
import com.example.domain.vo.TeacherDashboardStatsResponse;
import com.example.domain.vo.TeacherEnrollmentResponse;
import com.example.dto.user.UserProfileResponse;
import com.example.enums.UserRole;
import com.example.enums.UserStatus;
import com.example.exception.BusinessException;
import com.example.mapper.OrderMapper;
import com.example.mapper.OrderStatusLogMapper;
import com.example.mq.MqConstants;
import com.example.mq.OrderCreateMessage;
import com.example.mq.OrderTimeoutMessage;
import com.example.mq.PaymentSuccessMessage;
import com.example.service.OrderService;
import com.example.support.RedisCourseStockGuard;
import com.example.support.RedisSubmitLock;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final DateTimeFormatter ORDER_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Duration SUBMIT_LOCK_TTL = Duration.ofSeconds(10);
    private static final String ORDER_SUBMIT_LOCK_PREFIX = "baimaxt:order:submit:";
    private static final String TEACHER_ROLE = "TEACHER";

    private final OrderMapper orderMapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final UserClient userClient;
    private final CourseClient courseClient;
    private final RabbitTemplate rabbitTemplate;
    private final RedisSubmitLock redisSubmitLock;
    private final RedisCourseStockGuard redisCourseStockGuard;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderStatusLogMapper statusLogMapper,
                            UserClient userClient,
                            CourseClient courseClient,
                            RabbitTemplate rabbitTemplate,
                            RedisSubmitLock redisSubmitLock,
                            RedisCourseStockGuard redisCourseStockGuard) {
        this.orderMapper = orderMapper;
        this.statusLogMapper = statusLogMapper;
        this.userClient = userClient;
        this.courseClient = courseClient;
        this.rabbitTemplate = rabbitTemplate;
        this.redisSubmitLock = redisSubmitLock;
        this.redisCourseStockGuard = redisCourseStockGuard;
    }

    @Override
    public OrderSubmitResponse create(CreateOrderRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        if (request.userId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User id must not be null");
        }
        if (request.courseId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course id must not be null");
        }
        String lockKey = ORDER_SUBMIT_LOCK_PREFIX + request.userId() + ":" + request.courseId();
        String lockValue = redisSubmitLock.tryLock(lockKey, SUBMIT_LOCK_TTL);
        if (lockValue == null) {
            throw new BusinessException(HttpStatus.CONFLICT, "Please do not submit repeatedly");
        }
        try {
            UserProfileResponse user = requireEnabledUser(request.userId());
            CourseDetailClientResponse course = requireOnSaleCourse(request.courseId());
            requireNoActiveOrder(user.id(), course.id());
            String requestId = UUID.randomUUID().toString();
            OrderSubmitStatus reserveStatus = redisCourseStockGuard.reserve(
                    course.id(),
                    user.id(),
                    requestId,
                    course.availableStock()
            );
            if (reserveStatus != OrderSubmitStatus.QUEUEING) {
                return new OrderSubmitResponse(requestId, reserveStatus, null, null, submitStatusMessage(reserveStatus));
            }
            try {
                rabbitTemplate.convertAndSend(
                        MqConstants.ORDER_EXCHANGE,
                        MqConstants.ORDER_CREATE_ROUTING_KEY,
                        new OrderCreateMessage(requestId, user.id(), course.id(), request.remark())
                );
                return new OrderSubmitResponse(requestId, OrderSubmitStatus.QUEUEING, null, null, "Order is queueing");
            } catch (RuntimeException exception) {
                redisCourseStockGuard.releaseReservation(course.id(), user.id());
                redisCourseStockGuard.markFailure(requestId, OrderSubmitStatus.FAILED, "Failed to queue order");
                throw exception;
            }
        } finally {
            redisSubmitLock.release(lockKey, lockValue);
        }
    }

    @Override
    public OrderSubmitResponse getSubmitResult(String requestId) {
        String value = trimToNull(requestId);
        if (value == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request id must not be blank");
        }
        return redisCourseStockGuard.getResult(value);
    }

    @Override
    @Transactional
    public void createFromQueue(OrderCreateMessage message) {
        if (message == null || trimToNull(message.requestId()) == null
                || message.userId() == null || message.courseId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Order create message is invalid");
        }
        OrderSubmitResponse current = redisCourseStockGuard.getResult(message.requestId());
        if (current.status() == OrderSubmitStatus.SUCCESS) {
            return;
        }
        boolean dbStockReserved = false;
        try {
            UserProfileResponse user = requireEnabledUser(message.userId());
            CourseDetailClientResponse course = requireOnSaleCourse(message.courseId());
            Order activeOrder = findActiveOrder(user.id(), course.id());
            if (activeOrder != null) {
                redisCourseStockGuard.releaseReservation(course.id(), user.id());
                redisCourseStockGuard.markSuccess(message.requestId(), activeOrder.getId(), activeOrder.getOrderNo());
                return;
            }
            decreaseCourseStock(course.id());
            dbStockReserved = true;
            Order order = buildOrder(user, course, message.remark());
            orderMapper.insert(order);
            appendLog(order, null, order.getOrderStatus(), null, order.getPayStatus(),
                    OrderOperateType.CREATE_ORDER, user.id(), user.role(), "Create order");
            rabbitTemplate.convertAndSend(
                    MqConstants.ORDER_EXCHANGE,
                    MqConstants.ORDER_TIMEOUT_DELAY_ROUTING_KEY,
                    new OrderTimeoutMessage(order.getOrderNo(), order.getUserId(), order.getExpireTime())
            );
            redisCourseStockGuard.markSuccess(message.requestId(), order.getId(), order.getOrderNo());
        } catch (BusinessException exception) {
            if (dbStockReserved) {
                restoreCourseStock(message.courseId(), message.userId());
            } else if (exception.getStatus() == HttpStatus.CONFLICT) {
                redisCourseStockGuard.markSoldOut(message.courseId(), message.userId());
            } else {
                redisCourseStockGuard.releaseReservation(message.courseId(), message.userId());
            }
            redisCourseStockGuard.markFailure(message.requestId(), failureStatus(exception), exception.getMessage());
            throw exception;
        } catch (RuntimeException exception) {
            if (dbStockReserved) {
                restoreCourseStock(message.courseId(), message.userId());
            } else {
                redisCourseStockGuard.releaseReservation(message.courseId(), message.userId());
            }
            redisCourseStockGuard.markFailure(message.requestId(), OrderSubmitStatus.FAILED, "Failed to create order");
            throw exception;
        }
    }

    @Override
    public List<OrderResponse> listMine(Long userId) {
        requireEnabledUser(userId);
        return orderMapper.selectList(new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreatedAt)
                        .orderByDesc(Order::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<StudentCourseResponse> listMyCourses(Long userId) {
        requireEnabledUser(userId);
        return orderMapper.selectList(new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getOrderStatus, OrderStatus.PAID)
                        .eq(Order::getPayStatus, PayStatus.PAID)
                        .orderByDesc(Order::getPayTime)
                        .orderByDesc(Order::getId))
                .stream()
                .map(this::toStudentCourseResponse)
                .toList();
    }

    @Override
    public List<TeacherEnrollmentResponse> listTeacherEnrollments(Long userId, Long courseId) {
        TeacherClientResponse teacher = requireTeacherProfile(userId);
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<Order>()
                .eq(Order::getTeacherId, teacher.id())
                .eq(Order::getOrderStatus, OrderStatus.PAID)
                .eq(Order::getPayStatus, PayStatus.PAID)
                .orderByDesc(Order::getPayTime)
                .orderByDesc(Order::getId);
        if (courseId != null) {
            query.eq(Order::getCourseId, courseId);
        }
        return orderMapper.selectList(query).stream()
                .map(this::toTeacherEnrollmentResponse)
                .toList();
    }

    @Override
    public TeacherDashboardStatsResponse getTeacherDashboardStats(Long userId) {
        TeacherClientResponse teacher = requireTeacherProfile(userId);
        List<CourseListClientResponse> courses = listTeacherCourses(userId);
        List<Order> paidOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getTeacherId, teacher.id())
                .eq(Order::getOrderStatus, OrderStatus.PAID)
                .eq(Order::getPayStatus, PayStatus.PAID));
        Map<Long, List<Order>> ordersByCourse = paidOrders.stream()
                .collect(Collectors.groupingBy(Order::getCourseId));
        List<TeacherCourseStatsResponse> courseStats = courses.stream()
                .map(course -> toTeacherCourseStatsResponse(course, ordersByCourse.getOrDefault(course.id(), Collections.emptyList())))
                .toList();
        Set<Long> studentIds = paidOrders.stream()
                .map(Order::getUserId)
                .collect(Collectors.toSet());
        BigDecimal revenue = paidOrders.stream()
                .map(Order::getPayAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int onSaleCourses = (int) courses.stream()
                .filter(course -> "ON_SALE".equals(course.status()))
                .count();
        int availableStock = courses.stream()
                .map(CourseListClientResponse::availableStock)
                .filter(stock -> stock != null)
                .reduce(0, Integer::sum);
        return new TeacherDashboardStatsResponse(
                teacher.id(),
                courses.size(),
                onSaleCourses,
                (long) paidOrders.size(),
                (long) studentIds.size(),
                availableStock,
                revenue,
                courseStats
        );
    }

    @Override
    public OrderDetailResponse getDetail(Long id, Long userId) {
        Order order = getOrder(id);
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Order does not belong to user");
        }
        List<OrderStatusLogResponse> logs = statusLogMapper.selectList(new LambdaQueryWrapper<OrderStatusLog>()
                        .eq(OrderStatusLog::getOrderId, order.getId())
                        .orderByAsc(OrderStatusLog::getCreatedAt)
                        .orderByAsc(OrderStatusLog::getId))
                .stream()
                .map(this::toLogResponse)
                .toList();
        return new OrderDetailResponse(toResponse(order), logs);
    }

    @Override
    @Transactional
    public OrderResponse cancel(Long id, Long userId) {
        requireEnabledUser(userId);
        Order order = getOrder(id);
        requireOwner(order, userId);
        if (order.getOrderStatus() != OrderStatus.CREATED || order.getPayStatus() != PayStatus.UNPAID) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only unpaid created orders can be cancelled");
        }
        OrderStatus oldOrderStatus = order.getOrderStatus();
        PayStatus oldPayStatus = order.getPayStatus();
        LocalDateTime now = LocalDateTime.now();
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelTime(now);
        order.setUpdatedAt(now);
        orderMapper.updateById(order);
        restoreCourseStock(order.getCourseId(), order.getUserId());
        appendLog(order, oldOrderStatus, order.getOrderStatus(), oldPayStatus, order.getPayStatus(),
                OrderOperateType.USER_CANCEL, userId, UserRole.STUDENT, "User cancelled order");
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse mockPay(Long id, Long userId) {
        requireEnabledUser(userId);
        Order order = getOrder(id);
        requireOwner(order, userId);
        if (order.getOrderStatus() != OrderStatus.CREATED || order.getPayStatus() != PayStatus.UNPAID) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only unpaid created orders can be paid");
        }
        OrderStatus oldOrderStatus = order.getOrderStatus();
        PayStatus oldPayStatus = order.getPayStatus();
        LocalDateTime now = LocalDateTime.now();
        order.setOrderStatus(OrderStatus.PAID);
        order.setPayStatus(PayStatus.PAID);
        order.setPayType(PayType.MOCK);
        order.setPayTime(now);
        order.setUpdatedAt(now);
        orderMapper.updateById(order);
        appendLog(order, oldOrderStatus, order.getOrderStatus(), oldPayStatus, order.getPayStatus(),
                OrderOperateType.MOCK_PAY_SUCCESS, userId, UserRole.STUDENT, "Mock pay success");
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> listAdmin(Long userId, OrderStatus orderStatus, PayStatus payStatus) {
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreatedAt)
                .orderByDesc(Order::getId);
        if (userId != null) {
            query.eq(Order::getUserId, userId);
        }
        if (orderStatus != null) {
            query.eq(Order::getOrderStatus, orderStatus);
        }
        if (payStatus != null) {
            query.eq(Order::getPayStatus, payStatus);
        }
        return orderMapper.selectList(query).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderPayableResponse getPayable(String orderNo, Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User id must not be null");
        }
        Order order = getOrderByOrderNo(orderNo);
        requireOwner(order, userId);
        if (order.getOrderStatus() != OrderStatus.CREATED || order.getPayStatus() != PayStatus.UNPAID) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only unpaid created orders can be paid");
        }
        if (order.getExpireTime() != null && !order.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Order is expired");
        }
        return new OrderPayableResponse(
                order.getOrderNo(),
                order.getUserId(),
                order.getPayAmount(),
                order.getCourseTitle(),
                order.getCourseSubtitle(),
                order.getExpireTime()
        );
    }

    @Override
    @Transactional
    public OrderPaymentConfirmResponse confirmPayment(String orderNo, ConfirmPaymentRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        if (request.payNo() == null || request.payNo().trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Payment no must not be blank");
        }
        if (request.payType() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Pay type must not be null");
        }
        if (request.paidAmount() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Paid amount must not be null");
        }

        Order order = getOrderByOrderNo(orderNo);
        if (order.getPayAmount() == null || order.getPayAmount().compareTo(request.paidAmount()) != 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Paid amount does not match order amount");
        }
        if (order.getOrderStatus() == OrderStatus.PAID && order.getPayStatus() == PayStatus.PAID) {
            return toPaymentConfirmResponse(order);
        }
        if (order.getOrderStatus() != OrderStatus.CREATED || order.getPayStatus() != PayStatus.UNPAID) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only unpaid created orders can be confirmed");
        }
        if (order.getExpireTime() != null && !order.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Order is expired");
        }

        OrderStatus oldOrderStatus = order.getOrderStatus();
        PayStatus oldPayStatus = order.getPayStatus();
        LocalDateTime paidTime = request.paidTime() == null ? LocalDateTime.now() : request.paidTime();
        order.setOrderStatus(OrderStatus.PAID);
        order.setPayStatus(PayStatus.PAID);
        order.setPayType(request.payType());
        order.setPayTime(paidTime);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        appendLog(order, oldOrderStatus, order.getOrderStatus(), oldPayStatus, order.getPayStatus(),
                OrderOperateType.MOCK_PAY_SUCCESS, order.getUserId(), UserRole.STUDENT,
                "Payment confirmed: " + request.payNo().trim());
        return toPaymentConfirmResponse(order);
    }

    @Override
    @Transactional
    public void closeExpiredOrder(OrderTimeoutMessage message) {
        if (message == null || trimToNull(message.orderNo()) == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Order timeout message is invalid");
        }
        Order order = getOrderByOrderNo(message.orderNo());
        if (message.userId() != null && !order.getUserId().equals(message.userId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Order timeout user does not match");
        }
        if (order.getOrderStatus() == OrderStatus.PAID || order.getPayStatus() == PayStatus.PAID) {
            return;
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.EXPIRED) {
            return;
        }
        if (order.getOrderStatus() != OrderStatus.CREATED || order.getPayStatus() != PayStatus.UNPAID) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (order.getExpireTime() != null && order.getExpireTime().isAfter(now)) {
            return;
        }

        OrderStatus oldOrderStatus = order.getOrderStatus();
        PayStatus oldPayStatus = order.getPayStatus();
        order.setOrderStatus(OrderStatus.EXPIRED);
        order.setCancelTime(now);
        order.setUpdatedAt(now);
        orderMapper.updateById(order);
        restoreCourseStock(order.getCourseId(), order.getUserId());
        appendLog(order, oldOrderStatus, order.getOrderStatus(), oldPayStatus, order.getPayStatus(),
                OrderOperateType.SYSTEM_EXPIRE, order.getUserId(), UserRole.STUDENT, "Order expired");
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessMessage message) {
        if (message == null || trimToNull(message.payType()) == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Payment success message is invalid");
        }
        confirmPayment(message.orderNo(), new ConfirmPaymentRequest(
                message.payNo(),
                PayType.valueOf(message.payType()),
                message.paidAmount(),
                message.paidTime()
        ));
    }

    private UserProfileResponse requireEnabledUser(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User id must not be null");
        }
        try {
            ApiResponse<UserProfileResponse> response = userClient.getInternal(id);
            if (response == null || response.code() != 200 || response.data() == null) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to get user profile");
            }
            UserProfileResponse user = response.data();
            if (user.status() == UserStatus.DISABLED) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "User is disabled");
            }
            return user;
        } catch (FeignException.NotFound exception) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User does not exist");
        }
    }

    private CourseDetailClientResponse requireOnSaleCourse(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course id must not be null");
        }
        try {
            ApiResponse<CourseDetailClientResponse> response = courseClient.getAdminDetail(id);
            if (response == null || response.code() != 200 || response.data() == null) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to get course detail");
            }
            CourseDetailClientResponse course = response.data();
            if (course.status() != OrderCourseStatus.ON_SALE) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Course is not on sale");
            }
            return course;
        } catch (FeignException.NotFound exception) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Course does not exist");
        }
    }

    private TeacherClientResponse requireTeacherProfile(Long userId) {
        UserProfileResponse user = requireEnabledUser(userId);
        if (user.role() != UserRole.TEACHER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Teacher permission required");
        }
        try {
            ApiResponse<TeacherClientResponse> response = courseClient.getTeacherByUserId(user.id());
            if (response == null || response.code() != 200 || response.data() == null) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to get teacher profile");
            }
            return response.data();
        } catch (FeignException.NotFound exception) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Teacher profile does not exist");
        }
    }

    private List<CourseListClientResponse> listTeacherCourses(Long userId) {
        try {
            ApiResponse<List<CourseListClientResponse>> response = courseClient.listTeacherCourses(userId, TEACHER_ROLE);
            if (response == null || response.code() != 200 || response.data() == null) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to get teacher courses");
            }
            return response.data();
        } catch (FeignException.Forbidden exception) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Teacher permission required");
        }
    }

    private String studentDisplayName(Long userId) {
        try {
            ApiResponse<UserProfileResponse> response = userClient.getInternal(userId);
            if (response == null || response.code() != 200 || response.data() == null) {
                return "User " + userId;
            }
            UserProfileResponse user = response.data();
            String username = trimToNull(user.username());
            if (username != null) {
                return username;
            }
            String nickname = trimToNull(user.nickname());
            return nickname == null ? "User " + userId : nickname;
        } catch (FeignException exception) {
            return "User " + userId;
        }
    }

    private void requireNoActiveOrder(Long userId, Long courseId) {
        if (findActiveOrder(userId, courseId) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "Course already has an active order");
        }
    }

    private Order findActiveOrder(Long userId, Long courseId) {
        return orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getCourseId, courseId)
                .in(Order::getOrderStatus, List.of(OrderStatus.CREATED, OrderStatus.PAID))
                .last("LIMIT 1"));
    }

    private Order buildOrder(UserProfileResponse user, CourseDetailClientResponse course, String remark) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal price = requireMoney(course.price());
        BigDecimal originalAmount = course.originalPrice() == null ? price : course.originalPrice();
        BigDecimal discountAmount = originalAmount.compareTo(price) > 0 ? originalAmount.subtract(price) : BigDecimal.ZERO;
        TeacherClientResponse teacher = course.teacher();

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(user.id());
        order.setCourseId(course.id());
        order.setCourseTitle(course.title());
        order.setCourseSubtitle(course.subtitle());
        order.setTeacherId(teacher == null ? null : teacher.id());
        order.setTeacherName(teacher == null ? null : teacher.name());
        order.setOriginalAmount(originalAmount);
        order.setPayAmount(price);
        order.setDiscountAmount(discountAmount);
        order.setOrderStatus(OrderStatus.CREATED);
        order.setPayStatus(PayStatus.UNPAID);
        order.setPayType(PayType.MOCK);
        order.setExpireTime(now.plusMinutes(30));
        order.setRemark(trimToNull(remark));
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return order;
    }

    private void decreaseCourseStock(Long courseId) {
        try {
            ApiResponse<Void> response = courseClient.decreaseStock(courseId);
            if (response == null || response.code() != 200) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to reserve course stock");
            }
        } catch (FeignException.Conflict exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "Course stock is sold out");
        } catch (FeignException.BadRequest exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course is not available");
        } catch (FeignException.NotFound exception) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Course does not exist");
        }
    }

    private void restoreCourseStock(Long courseId, Long userId) {
        try {
            ApiResponse<Void> response = courseClient.restoreStock(courseId);
            if (response == null || response.code() != 200) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to restore course stock");
            }
            redisCourseStockGuard.restoreAvailableStock(courseId);
            if (userId != null) {
                redisCourseStockGuard.removeBuyer(courseId, userId);
            }
        } catch (FeignException.NotFound exception) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Course does not exist");
        }
    }

    private OrderSubmitStatus failureStatus(BusinessException exception) {
        if (exception.getStatus() == HttpStatus.CONFLICT) {
            return OrderSubmitStatus.SOLD_OUT;
        }
        return OrderSubmitStatus.FAILED;
    }

    private String submitStatusMessage(OrderSubmitStatus status) {
        return switch (status) {
            case QUEUEING -> "Order is queueing";
            case SUCCESS -> "Order created";
            case SOLD_OUT -> "Course stock is sold out";
            case DUPLICATE -> "Course already has an active order";
            case FAILED -> "Failed to submit order";
        };
    }

    private Order getOrder(Long id) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Order id must not be null");
        }
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Order does not exist");
        }
        return order;
    }

    private Order getOrderByOrderNo(String orderNo) {
        String value = trimToNull(orderNo);
        if (value == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Order no must not be blank");
        }
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, value)
                .last("LIMIT 1"));
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Order does not exist");
        }
        return order;
    }

    private void requireOwner(Order order, Long userId) {
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Order does not belong to user");
        }
    }

    private BigDecimal requireMoney(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Course price is invalid");
        }
        return value;
    }

    private void appendLog(Order order,
                           OrderStatus oldOrderStatus,
                           OrderStatus newOrderStatus,
                           PayStatus oldPayStatus,
                           PayStatus newPayStatus,
                           OrderOperateType operateType,
                           Long operateBy,
                           UserRole operateRole,
                           String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setOldOrderStatus(oldOrderStatus);
        log.setNewOrderStatus(newOrderStatus);
        log.setOldPayStatus(oldPayStatus);
        log.setNewPayStatus(newPayStatus);
        log.setOperateType(operateType);
        log.setOperateBy(operateBy);
        log.setOperateRole(operateRole);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        statusLogMapper.insert(log);
    }

    private String generateOrderNo() {
        String date = LocalDate.now().format(ORDER_NO_DATE);
        String millis = String.valueOf(System.currentTimeMillis());
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "BM" + date + millis.substring(millis.length() - 8) + random;
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getUserId(),
                order.getCourseId(),
                order.getCourseTitle(),
                order.getCourseSubtitle(),
                order.getTeacherId(),
                order.getTeacherName(),
                order.getOriginalAmount(),
                order.getPayAmount(),
                order.getDiscountAmount(),
                order.getOrderStatus(),
                order.getPayStatus(),
                order.getPayType(),
                order.getPayTime(),
                order.getCancelTime(),
                order.getExpireTime(),
                order.getRemark(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderStatusLogResponse toLogResponse(OrderStatusLog log) {
        return new OrderStatusLogResponse(
                log.getId(),
                log.getOrderId(),
                log.getOrderNo(),
                log.getOldOrderStatus(),
                log.getNewOrderStatus(),
                log.getOldPayStatus(),
                log.getNewPayStatus(),
                log.getOperateType(),
                log.getOperateBy(),
                log.getOperateRole(),
                log.getRemark(),
                log.getCreatedAt()
        );
    }

    private StudentCourseResponse toStudentCourseResponse(Order order) {
        return new StudentCourseResponse(
                order.getId(),
                order.getOrderNo(),
                order.getCourseId(),
                order.getCourseTitle(),
                order.getCourseSubtitle(),
                order.getTeacherId(),
                order.getTeacherName(),
                order.getPayAmount(),
                order.getPayTime(),
                order.getCreatedAt()
        );
    }

    private TeacherEnrollmentResponse toTeacherEnrollmentResponse(Order order) {
        return new TeacherEnrollmentResponse(
                order.getId(),
                order.getOrderNo(),
                order.getUserId(),
                studentDisplayName(order.getUserId()),
                order.getCourseId(),
                order.getCourseTitle(),
                order.getCourseSubtitle(),
                order.getPayAmount(),
                order.getPayTime(),
                order.getCreatedAt()
        );
    }

    private TeacherCourseStatsResponse toTeacherCourseStatsResponse(CourseListClientResponse course, List<Order> paidOrders) {
        BigDecimal revenue = paidOrders.stream()
                .map(Order::getPayAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TeacherCourseStatsResponse(
                course.id(),
                course.title(),
                course.subtitle(),
                course.stock(),
                course.soldCount(),
                course.availableStock(),
                (long) paidOrders.size(),
                revenue
        );
    }

    private OrderPaymentConfirmResponse toPaymentConfirmResponse(Order order) {
        return new OrderPaymentConfirmResponse(
                order.getOrderNo(),
                order.getUserId(),
                order.getPayAmount(),
                order.getPayStatus(),
                order.getOrderStatus(),
                order.getPayTime()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
