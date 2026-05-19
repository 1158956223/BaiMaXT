package com.example.auth;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TokenStateService {

    private static final String SESSION_KEY_PREFIX = "baimaxt:auth:session:";
    private static final String BLACKLIST_KEY_PREFIX = "baimaxt:auth:blacklist:";

    private final ReactiveStringRedisTemplate redisTemplate;

    public TokenStateService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> isActive(JwtPayload payload) {
        String sessionKey = SESSION_KEY_PREFIX + payload.jti();
        String blacklistKey = BLACKLIST_KEY_PREFIX + payload.jti();
        return redisTemplate.hasKey(blacklistKey)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return Mono.just(false);
                    }
                    return redisTemplate.hasKey(sessionKey);
                })
                .defaultIfEmpty(false)
                .onErrorReturn(false);
    }
}
