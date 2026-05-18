package com.example.filter;

import com.example.auth.JwtPayload;
import com.example.auth.JwtValidator;
import com.example.support.GatewayResponseWriter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_ROLE = "ADMIN";

    private final JwtValidator jwtValidator;
    private final GatewayResponseWriter responseWriter;

    public AuthGatewayFilter(JwtValidator jwtValidator, GatewayResponseWriter responseWriter) {
        this.jwtValidator = jwtValidator;
        this.responseWriter = responseWriter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(request.getMethod()) || isPublicPath(request)) {
            return chain.filter(exchange);
        }
        if (isInternalPath(path)) {
            return responseWriter.write(exchange, HttpStatus.FORBIDDEN, "Forbidden");
        }

        String token = extractToken(request);
        if (token == null) {
            return responseWriter.write(exchange, HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        JwtPayload payload;
        try {
            payload = jwtValidator.validate(token);
        } catch (IllegalArgumentException exception) {
            return responseWriter.write(exchange, HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        if (path.startsWith("/api/admin/") && !ADMIN_ROLE.equals(payload.role())) {
            return responseWriter.write(exchange, HttpStatus.FORBIDDEN, "Forbidden");
        }

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Account-Id", payload.accountId().toString())
                .header("X-User-Id", payload.userId().toString())
                .header("X-Username", payload.username())
                .header("X-User-Role", payload.role())
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();
        return HttpMethod.POST.equals(method)
                && ("/api/auth/login".equals(path) || "/api/auth/register".equals(path));
    }

    private boolean isInternalPath(String path) {
        return path.startsWith("/api/users/internal/")
                || path.equals("/api/users/internal")
                || path.startsWith("/api/teachers/internal/")
                || path.equals("/api/teachers/internal")
                || path.startsWith("/api/orders/internal/")
                || path.equals("/api/orders/internal")
                || path.startsWith("/api/payments/internal/")
                || path.equals("/api/payments/internal");
    }

    private String extractToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
