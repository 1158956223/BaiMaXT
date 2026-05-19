package com.example.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisCacheConfig {

    public static final String COURSE_PUBLIC_LIST_CACHE = "course:public:list";
    public static final String COURSE_PUBLIC_DETAIL_CACHE = "course:public:detail";
    public static final String COURSE_CATEGORY_ENABLED_CACHE = "course:category:enabled";
    public static final String COURSE_TEACHER_ENABLED_CACHE = "course:teacher:enabled";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(ObjectMapper objectMapper) {
        RedisCacheConfiguration listConfig = cacheConfiguration(objectMapper, Duration.ofMinutes(10));
        RedisCacheConfiguration detailConfig = cacheConfiguration(objectMapper, Duration.ofMinutes(15));
        Map<String, RedisCacheConfiguration> configurations = new HashMap<>();
        configurations.put(COURSE_PUBLIC_LIST_CACHE, listConfig);
        configurations.put(COURSE_CATEGORY_ENABLED_CACHE, listConfig);
        configurations.put(COURSE_TEACHER_ENABLED_CACHE, listConfig);
        configurations.put(COURSE_PUBLIC_DETAIL_CACHE, detailConfig);
        return builder -> builder
                .cacheDefaults(listConfig)
                .withInitialCacheConfigurations(configurations);
    }

    @Bean
    public KeyGenerator courseCacheKeyGenerator() {
        return (target, method, params) -> {
            if (params.length == 0) {
                return "all";
            }
            StringBuilder key = new StringBuilder(method.getName());
            for (Object param : params) {
                key.append(':').append(param == null ? "null" : param.toString().trim());
            }
            return key.toString();
        };
    }

    private RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper, Duration ttl) {
        ObjectMapper redisObjectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        redisObjectMapper.activateDefaultTyping(
                redisObjectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}
