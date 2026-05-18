package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.api.ApiResponse;
import com.example.client.OrderClient;
import com.example.client.OrderClient.OrderPayableResponse;
import com.example.domain.dto.CreatePaymentRequest;
import com.example.domain.enums.PayFlowType;
import com.example.domain.enums.PayStatus;
import com.example.domain.enums.PayType;
import com.example.domain.po.PayFlow;
import com.example.domain.po.PayOrder;
import com.example.domain.vo.PaymentDetailResponse;
import com.example.domain.vo.PaymentFlowResponse;
import com.example.domain.vo.PaymentResponse;
import com.example.exception.BusinessException;
import com.example.mapper.PayFlowMapper;
import com.example.mapper.PayOrderMapper;
import com.example.mq.MqConstants;
import com.example.mq.PaymentSuccessMessage;
import com.example.service.PaymentService;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements PaymentService {

    private static final DateTimeFormatter PAY_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PayOrderMapper payOrderMapper;
    private final PayFlowMapper payFlowMapper;
    private final OrderClient orderClient;
    private final RabbitTemplate rabbitTemplate;

    public PaymentServiceImpl(PayOrderMapper payOrderMapper,
                              PayFlowMapper payFlowMapper,
                              OrderClient orderClient,
                              RabbitTemplate rabbitTemplate) {
        this.payOrderMapper = payOrderMapper;
        this.payFlowMapper = payFlowMapper;
        this.orderClient = orderClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public PaymentResponse create(CreatePaymentRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        String orderNo = trimToNull(request.orderNo());
        if (orderNo == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Order no must not be blank");
        }
        if (request.userId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User id must not be null");
        }
        PayType payType = request.payType() == null ? PayType.MOCK : request.payType();
        if (payType != PayType.MOCK) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only mock payment is supported now");
        }

        PayOrder successPayment = findLatest(orderNo, request.userId(), PayStatus.SUCCESS);
        if (successPayment != null) {
            return toResponse(successPayment);
        }

        PayOrder waitingPayment = findLatest(orderNo, request.userId(), PayStatus.WAITING);
        LocalDateTime now = LocalDateTime.now();
        if (waitingPayment != null) {
            if (waitingPayment.getExpireTime().isAfter(now)) {
                return toResponse(waitingPayment);
            }
            closeExpired(waitingPayment, now);
        }

        OrderPayableResponse order = requirePayableOrder(orderNo, request.userId());
        PayOrder payment = new PayOrder();
        payment.setPayNo(generatePayNo());
        payment.setOrderNo(order.orderNo());
        payment.setUserId(order.userId());
        payment.setPayAmount(requireMoney(order.payAmount()));
        payment.setPayType(payType);
        payment.setPayStatus(PayStatus.WAITING);
        payment.setSubject(trimToNull(order.subject()));
        payment.setDescription(trimToNull(order.description()));
        payment.setExpireTime(order.expireTime() == null ? now.plusMinutes(30) : order.expireTime());
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        payOrderMapper.insert(payment);
        appendFlow(payment, PayFlowType.CREATE, "Create payment", null);
        return toResponse(payment);
    }

    @Override
    public PaymentDetailResponse getDetail(String payNo, Long userId) {
        PayOrder payment = getPayment(payNo);
        if (userId != null && !payment.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Payment does not belong to user");
        }
        List<PaymentFlowResponse> flows = payFlowMapper.selectList(new LambdaQueryWrapper<PayFlow>()
                        .eq(PayFlow::getPayNo, payment.getPayNo())
                        .orderByAsc(PayFlow::getCreatedAt)
                        .orderByAsc(PayFlow::getId))
                .stream()
                .map(this::toFlowResponse)
                .toList();
        return new PaymentDetailResponse(toResponse(payment), flows);
    }

    @Override
    @Transactional
    public PaymentResponse mockSuccess(String payNo, Long userId) {
        PayOrder payment = requireOwnedPayment(payNo, userId);
        if (payment.getPayStatus() == PayStatus.SUCCESS) {
            return toResponse(payment);
        }
        if (payment.getPayStatus() != PayStatus.WAITING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only waiting payments can be paid");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!payment.getExpireTime().isAfter(now)) {
            closeExpired(payment, now);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Payment is expired");
        }

        String tradeNo = generateMockTradeNo();
        payment.setPayStatus(PayStatus.SUCCESS);
        payment.setTradeNo(tradeNo);
        payment.setPayTime(now);
        payment.setUpdatedAt(now);
        payOrderMapper.updateById(payment);
        appendFlow(payment, PayFlowType.SUCCESS, "Mock pay success", "mock success");
        publishPaymentSuccess(payment);
        return toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse mockFail(String payNo, Long userId) {
        PayOrder payment = requireOwnedPayment(payNo, userId);
        if (payment.getPayStatus() == PayStatus.FAILED) {
            return toResponse(payment);
        }
        if (payment.getPayStatus() != PayStatus.WAITING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only waiting payments can fail");
        }
        LocalDateTime now = LocalDateTime.now();
        payment.setPayStatus(PayStatus.FAILED);
        payment.setUpdatedAt(now);
        payOrderMapper.updateById(payment);
        appendFlow(payment, PayFlowType.FAIL, "Mock pay failed", "mock fail");
        return toResponse(payment);
    }

    @Override
    public List<PaymentResponse> listMine(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User id must not be null");
        }
        return payOrderMapper.selectList(new LambdaQueryWrapper<PayOrder>()
                        .eq(PayOrder::getUserId, userId)
                        .orderByDesc(PayOrder::getCreatedAt)
                        .orderByDesc(PayOrder::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<PaymentResponse> listAdmin(Long userId, PayStatus payStatus) {
        LambdaQueryWrapper<PayOrder> query = new LambdaQueryWrapper<PayOrder>()
                .orderByDesc(PayOrder::getCreatedAt)
                .orderByDesc(PayOrder::getId);
        if (userId != null) {
            query.eq(PayOrder::getUserId, userId);
        }
        if (payStatus != null) {
            query.eq(PayOrder::getPayStatus, payStatus);
        }
        return payOrderMapper.selectList(query).stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderPayableResponse requirePayableOrder(String orderNo, Long userId) {
        try {
            ApiResponse<OrderPayableResponse> response = orderClient.getPayable(orderNo, userId);
            if (response == null || response.code() != 200 || response.data() == null) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to get payable order");
            }
            return response.data();
        } catch (FeignException.NotFound exception) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Order does not exist");
        } catch (FeignException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Failed to get payable order");
        }
    }

    private void publishPaymentSuccess(PayOrder payment) {
        rabbitTemplate.convertAndSend(
                MqConstants.PAY_EXCHANGE,
                MqConstants.PAYMENT_SUCCESS_ROUTING_KEY,
                new PaymentSuccessMessage(
                        payment.getOrderNo(),
                        payment.getPayNo(),
                        payment.getPayType().name(),
                        payment.getPayAmount(),
                        payment.getPayTime()
                )
        );
    }

    private PayOrder requireOwnedPayment(String payNo, Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User id must not be null");
        }
        PayOrder payment = getPayment(payNo);
        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Payment does not belong to user");
        }
        return payment;
    }

    private PayOrder getPayment(String payNo) {
        String value = trimToNull(payNo);
        if (value == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Payment no must not be blank");
        }
        PayOrder payment = payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getPayNo, value)
                .last("LIMIT 1"));
        if (payment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Payment does not exist");
        }
        return payment;
    }

    private PayOrder findLatest(String orderNo, Long userId, PayStatus payStatus) {
        return payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getOrderNo, orderNo)
                .eq(PayOrder::getUserId, userId)
                .eq(PayOrder::getPayStatus, payStatus)
                .orderByDesc(PayOrder::getCreatedAt)
                .orderByDesc(PayOrder::getId)
                .last("LIMIT 1"));
    }

    private void closeExpired(PayOrder payment, LocalDateTime now) {
        payment.setPayStatus(PayStatus.CLOSED);
        payment.setCloseTime(now);
        payment.setUpdatedAt(now);
        payOrderMapper.updateById(payment);
        appendFlow(payment, PayFlowType.CLOSE, "Payment expired", null);
    }

    private void appendFlow(PayOrder payment, PayFlowType flowType, String remark, String rawContent) {
        PayFlow flow = new PayFlow();
        flow.setPayNo(payment.getPayNo());
        flow.setOrderNo(payment.getOrderNo());
        flow.setUserId(payment.getUserId());
        flow.setFlowType(flowType);
        flow.setPayType(payment.getPayType());
        flow.setAmount(payment.getPayAmount());
        flow.setTradeNo(payment.getTradeNo());
        flow.setRemark(remark);
        flow.setRawContent(rawContent);
        flow.setCreatedAt(LocalDateTime.now());
        payFlowMapper.insert(flow);
    }

    private BigDecimal requireMoney(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Order pay amount is invalid");
        }
        return value;
    }

    private String generatePayNo() {
        String date = LocalDate.now().format(PAY_NO_DATE);
        String millis = String.valueOf(System.currentTimeMillis());
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "PAY" + date + millis.substring(millis.length() - 8) + random;
    }

    private String generateMockTradeNo() {
        String date = LocalDate.now().format(PAY_NO_DATE);
        int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "MOCK" + date + System.currentTimeMillis() + random;
    }

    private PaymentResponse toResponse(PayOrder payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPayNo(),
                payment.getOrderNo(),
                payment.getUserId(),
                payment.getPayAmount(),
                payment.getPayType(),
                payment.getPayStatus(),
                payment.getSubject(),
                payment.getDescription(),
                payment.getTradeNo(),
                payment.getExpireTime(),
                payment.getPayTime(),
                payment.getCloseTime(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    private PaymentFlowResponse toFlowResponse(PayFlow flow) {
        return new PaymentFlowResponse(
                flow.getId(),
                flow.getPayNo(),
                flow.getOrderNo(),
                flow.getUserId(),
                flow.getFlowType(),
                flow.getPayType(),
                flow.getAmount(),
                flow.getTradeNo(),
                flow.getRemark(),
                flow.getRawContent(),
                flow.getCreatedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
