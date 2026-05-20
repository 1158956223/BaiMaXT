package com.example.mq;

import com.example.exception.BusinessException;
import com.example.service.CourseSearchService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CourseIndexMessageListener {

    private static final Logger log = LoggerFactory.getLogger(CourseIndexMessageListener.class);

    private final CourseSearchService courseSearchService;

    public CourseIndexMessageListener(CourseSearchService courseSearchService) {
        this.courseSearchService = courseSearchService;
    }

    @RabbitListener(queues = MqConstants.SEARCH_COURSE_INDEX_QUEUE)
    public void handleCourseIndexChanged(CourseIndexMessage message) {
        try {
            CourseIndexEventType eventType = validate(message);
            courseSearchService.syncCourse(message.courseId());
            log.info("Course search index synced, courseId={}, eventType={}", message.courseId(), eventType);
        } catch (BusinessException exception) {
            if (exception.getStatus() == HttpStatus.NOT_FOUND) {
                Long courseId = message == null ? null : message.courseId();
                if (courseId != null) {
                    courseSearchService.deleteCourse(courseId);
                    log.warn("Course not available while syncing search index, deleted document, courseId={}", courseId, exception);
                    return;
                }
            }
            log.warn("Reject invalid course index message: {}", message, exception);
            throw new AmqpRejectAndDontRequeueException(exception);
        } catch (FeignException.NotFound exception) {
            Long courseId = message == null ? null : message.courseId();
            if (courseId != null) {
                courseSearchService.deleteCourse(courseId);
                log.warn("Course not found while syncing search index, deleted document, courseId={}", courseId, exception);
                return;
            }
            throw new AmqpRejectAndDontRequeueException(exception);
        } catch (IllegalArgumentException exception) {
            log.warn("Reject invalid course index event type: {}", message, exception);
            throw new AmqpRejectAndDontRequeueException(exception);
        }
    }

    private CourseIndexEventType validate(CourseIndexMessage message) {
        if (message == null || message.courseId() == null || message.eventType() == null || message.eventType().trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Course index message is invalid");
        }
        return CourseIndexEventType.valueOf(message.eventType());
    }
}
