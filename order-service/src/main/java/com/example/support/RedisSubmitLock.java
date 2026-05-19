package com.example.support;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisSubmitLock {

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public RedisSubmitLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String tryLock(String key, Duration ttl) {
        String value = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
        return Boolean.TRUE.equals(locked) ? value : null;
    }

    public void release(String key, String value) {
        if (value == null) {
            return;
        }
        redisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(key), value);
    }
}
