package com.example.service;

import com.example.exception.BusinessException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TokenSessionService {

    private static final String SESSION_KEY_PREFIX = "baimaxt:auth:session:";
    private static final String BLACKLIST_KEY_PREFIX = "baimaxt:auth:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public TokenSessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void registerSession(String jti, Long accountId, long expirationSeconds) {
        redisTemplate.opsForValue().set(
                SESSION_KEY_PREFIX + jti,
                accountId == null ? "" : accountId.toString(),
                Duration.ofSeconds(expirationSeconds)
        );
    }

    public void requireActive(Map<String, Object> payload) {
        String jti = stringClaim(payload, "jti");
        if (redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + jti)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (!redisTemplate.hasKey(SESSION_KEY_PREFIX + jti)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    public void blacklist(Map<String, Object> payload) {
        String jti = stringClaim(payload, "jti");
        long exp = longClaim(payload, "exp");
        long ttlSeconds = exp - Instant.now().getEpochSecond();
        redisTemplate.delete(SESSION_KEY_PREFIX + jti);
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_KEY_PREFIX + jti, "1", Duration.ofSeconds(ttlSeconds));
        }
    }

    private String stringClaim(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return value.toString();
    }

    private long longClaim(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return Long.parseLong(value.toString());
    }
}
