package com.example.config;

import com.example.mq.MqConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class OrderRabbitConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(MqConstants.ORDER_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange payExchange() {
        return new DirectExchange(MqConstants.PAY_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderTimeoutDelayQueue() {
        return QueueBuilder.durable(MqConstants.ORDER_TIMEOUT_DELAY_QUEUE)
                .ttl(MqConstants.ORDER_TIMEOUT_TTL_MILLIS)
                .deadLetterExchange(MqConstants.ORDER_EXCHANGE)
                .deadLetterRoutingKey(MqConstants.ORDER_TIMEOUT_CLOSE_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderTimeoutCloseQueue() {
        return QueueBuilder.durable(MqConstants.ORDER_TIMEOUT_CLOSE_QUEUE).build();
    }

    @Bean
    public Queue orderCreateQueue() {
        return QueueBuilder.durable(MqConstants.ORDER_CREATE_QUEUE).build();
    }

    @Bean
    public Queue orderPaymentSuccessQueue() {
        return QueueBuilder.durable(MqConstants.ORDER_PAYMENT_SUCCESS_QUEUE).build();
    }

    @Bean
    public Binding orderTimeoutDelayBinding(@Qualifier("orderTimeoutDelayQueue") Queue orderTimeoutDelayQueue,
                                            @Qualifier("orderExchange") DirectExchange orderExchange) {
        return BindingBuilder.bind(orderTimeoutDelayQueue)
                .to(orderExchange)
                .with(MqConstants.ORDER_TIMEOUT_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding orderTimeoutCloseBinding(@Qualifier("orderTimeoutCloseQueue") Queue orderTimeoutCloseQueue,
                                            @Qualifier("orderExchange") DirectExchange orderExchange) {
        return BindingBuilder.bind(orderTimeoutCloseQueue)
                .to(orderExchange)
                .with(MqConstants.ORDER_TIMEOUT_CLOSE_ROUTING_KEY);
    }

    @Bean
    public Binding orderCreateBinding(@Qualifier("orderCreateQueue") Queue orderCreateQueue,
                                      @Qualifier("orderExchange") DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCreateQueue)
                .to(orderExchange)
                .with(MqConstants.ORDER_CREATE_ROUTING_KEY);
    }

    @Bean
    public Binding orderPaymentSuccessBinding(@Qualifier("orderPaymentSuccessQueue") Queue orderPaymentSuccessQueue,
                                              @Qualifier("payExchange") DirectExchange payExchange) {
        return BindingBuilder.bind(orderPaymentSuccessQueue)
                .to(payExchange)
                .with(MqConstants.PAYMENT_SUCCESS_ROUTING_KEY);
    }
}
