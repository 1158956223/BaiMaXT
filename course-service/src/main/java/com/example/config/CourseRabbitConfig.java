package com.example.config;

import com.example.mq.MqConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourseRabbitConfig {

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
    public DirectExchange courseExchange() {
        return new DirectExchange(MqConstants.COURSE_EXCHANGE, true, false);
    }

    @Bean
    public Queue searchCourseIndexQueue() {
        return QueueBuilder.durable(MqConstants.SEARCH_COURSE_INDEX_QUEUE).build();
    }

    @Bean
    public Binding searchCourseIndexBinding(@Qualifier("searchCourseIndexQueue") Queue searchCourseIndexQueue,
                                            @Qualifier("courseExchange") DirectExchange courseExchange) {
        return BindingBuilder.bind(searchCourseIndexQueue)
                .to(courseExchange)
                .with(MqConstants.COURSE_INDEX_CHANGED_ROUTING_KEY);
    }
}
