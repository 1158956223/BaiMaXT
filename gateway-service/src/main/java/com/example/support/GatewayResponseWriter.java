package com.example.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GatewayResponseWriter {

    private final ObjectMapper objectMapper;

    public GatewayResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = toJson(new GatewayApiResponse<>(status.value(), message, null));
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    private byte[] toJson(GatewayApiResponse<?> response) {
        try {
            return objectMapper.writeValueAsBytes(response);
        } catch (JsonProcessingException exception) {
            return "{\"code\":500,\"message\":\"Gateway error\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        }
    }

    private record GatewayApiResponse<T>(int code, String message, T data) {
    }
}
