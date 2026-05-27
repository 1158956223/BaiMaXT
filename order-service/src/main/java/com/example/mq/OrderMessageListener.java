package com.example.mq;

import com.example.exception.BusinessException;
import com.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderMessageListener {

    private static final Logger log = LoggerFactory.getLogger(OrderMessageListener.class);

    private final OrderService orderService;

    public OrderMessageListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = MqConstants.ORDER_CREATE_QUEUE)
    public void handleOrderCreate(OrderCreateMessage message) {
        try {
            orderService.createFromQueue(message);
        } catch (BusinessException exception) {
            log.warn("Reject invalid order create message: {}", message, exception);
            throw new AmqpRejectAndDontRequeueException(exception);
        }
    }

    @RabbitListener(queues = MqConstants.ORDER_TIMEOUT_CLOSE_QUEUE)
    public void handleOrderTimeout(OrderTimeoutMessage message) {
        try {
            orderService.closeExpiredOrder(message);
        } catch (BusinessException exception) {
            log.warn("Reject invalid order timeout message: {}", message, exception);
            throw new AmqpRejectAndDontRequeueException(exception);
        }
    }

    @RabbitListener(queues = MqConstants.ORDER_PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(PaymentSuccessMessage message) {
        try {
            orderService.handlePaymentSuccess(message);
        } catch (BusinessException | IllegalArgumentException exception) {
            log.warn("Reject invalid payment success message: {}", message, exception);
            throw new AmqpRejectAndDontRequeueException(exception);
        }
    }
}
