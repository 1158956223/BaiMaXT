package com.example.mq;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CourseIndexMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(CourseIndexMessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public CourseIndexMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Long courseId, CourseIndexEventType eventType) {
        try {
            rabbitTemplate.convertAndSend(
                    MqConstants.COURSE_EXCHANGE,
                    MqConstants.COURSE_INDEX_CHANGED_ROUTING_KEY,
                    new CourseIndexMessage(courseId, eventType.name(), LocalDateTime.now())
            );
        } catch (AmqpException exception) {
            log.warn("Failed to publish course index message, courseId={}, eventType={}", courseId, eventType, exception);
        }
    }
}
