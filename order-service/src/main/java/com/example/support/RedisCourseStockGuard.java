package com.example.support;

import com.example.domain.enums.OrderSubmitStatus;
import com.example.domain.vo.OrderSubmitResponse;
import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisCourseStockGuard {

    private static final Duration BUYER_TTL = Duration.ofHours(2);
    private static final Duration RESULT_TTL = Duration.ofMinutes(30);
    private static final String STOCK_PREFIX = "baimaxt:course:stock:";
    private static final String BUYERS_PREFIX = "baimaxt:course:buyers:";
    private static final String RESULT_PREFIX = "baimaxt:order:submit:result:";

    private static final DefaultRedisScript<String> RESERVE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
                redis.call('set', KEYS[3], 'DUPLICATE|||Course already has an active order', 'EX', ARGV[4])
                return 'DUPLICATE'
            end
            if redis.call('exists', KEYS[1]) == 0 then
                redis.call('set', KEYS[1], ARGV[2])
            end
            local current = tonumber(redis.call('get', KEYS[1]) or '0')
            if current <= 0 then
                redis.call('set', KEYS[3], 'SOLD_OUT|||Course stock is sold out', 'EX', ARGV[4])
                return 'SOLD_OUT'
            end
            redis.call('decr', KEYS[1])
            redis.call('sadd', KEYS[2], ARGV[1])
            redis.call('expire', KEYS[2], ARGV[3])
            redis.call('set', KEYS[3], 'QUEUEING|||Order is queueing', 'EX', ARGV[4])
            return 'QUEUEING'
            """, String.class);

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('exists', KEYS[1]) == 1 then
                redis.call('incr', KEYS[1])
            end
            return redis.call('srem', KEYS[2], ARGV[1])
            """, Long.class);

    private static final DefaultRedisScript<Long> RESTORE_STOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('exists', KEYS[1]) == 1 then
                return redis.call('incr', KEYS[1])
            end
            return 0
            """, Long.class);

    private static final DefaultRedisScript<Long> SOLD_OUT_SCRIPT = new DefaultRedisScript<>("""
            redis.call('set', KEYS[1], '0')
            return redis.call('srem', KEYS[2], ARGV[1])
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisCourseStockGuard(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public OrderSubmitStatus reserve(Long courseId, Long userId, String requestId, Integer availableStock) {
        String status = redisTemplate.execute(
                RESERVE_SCRIPT,
                List.of(stockKey(courseId), buyersKey(courseId), resultKey(requestId)),
                String.valueOf(userId),
                String.valueOf(Math.max(availableStock == null ? 0 : availableStock, 0)),
                String.valueOf(BUYER_TTL.toSeconds()),
                String.valueOf(RESULT_TTL.toSeconds())
        );
        return OrderSubmitStatus.valueOf(status == null ? OrderSubmitStatus.FAILED.name() : status);
    }

    public void releaseReservation(Long courseId, Long userId) {
        redisTemplate.execute(RELEASE_SCRIPT, List.of(stockKey(courseId), buyersKey(courseId)), String.valueOf(userId));
    }

    public void restoreAvailableStock(Long courseId) {
        redisTemplate.execute(RESTORE_STOCK_SCRIPT, List.of(stockKey(courseId)));
    }

    public void markSoldOut(Long courseId, Long userId) {
        redisTemplate.execute(SOLD_OUT_SCRIPT, List.of(stockKey(courseId), buyersKey(courseId)), String.valueOf(userId));
    }

    public void removeBuyer(Long courseId, Long userId) {
        redisTemplate.opsForSet().remove(buyersKey(courseId), String.valueOf(userId));
    }

    public void markSuccess(String requestId, Long orderId, String orderNo) {
        saveResult(requestId, OrderSubmitStatus.SUCCESS, orderId, orderNo, "Order created");
    }

    public void markFailure(String requestId, OrderSubmitStatus status, String message) {
        saveResult(requestId, status, null, null, message);
    }

    public OrderSubmitResponse getResult(String requestId) {
        String value = redisTemplate.opsForValue().get(resultKey(requestId));
        if (value == null) {
            return new OrderSubmitResponse(requestId, OrderSubmitStatus.FAILED, null, null, "Order submit result expired");
        }
        return parseResult(requestId, value);
    }

    private void saveResult(String requestId, OrderSubmitStatus status, Long orderId, String orderNo, String message) {
        redisTemplate.opsForValue().set(
                resultKey(requestId),
                status.name() + "|" + nullToEmpty(orderId) + "|" + nullToEmpty(orderNo) + "|" + nullToEmpty(message),
                RESULT_TTL
        );
    }

    private OrderSubmitResponse parseResult(String requestId, String value) {
        String[] parts = value.split("\\|", -1);
        OrderSubmitStatus status = OrderSubmitStatus.valueOf(parts[0]);
        Long orderId = parts.length > 1 && !parts[1].isBlank() ? Long.valueOf(parts[1]) : null;
        String orderNo = parts.length > 2 && !parts[2].isBlank() ? parts[2] : null;
        String message = parts.length > 3 && !parts[3].isBlank() ? parts[3] : null;
        return new OrderSubmitResponse(requestId, status, orderId, orderNo, message);
    }

    private String stockKey(Long courseId) {
        return STOCK_PREFIX + courseId;
    }

    private String buyersKey(Long courseId) {
        return BUYERS_PREFIX + courseId;
    }

    private String resultKey(String requestId) {
        return RESULT_PREFIX + requestId;
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}
