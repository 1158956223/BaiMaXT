package com.example.support;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final GatewayResponseWriter responseWriter;

    public GatewayExceptionHandler(GatewayResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        return responseWriter.write(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Gateway error");
    }
}
