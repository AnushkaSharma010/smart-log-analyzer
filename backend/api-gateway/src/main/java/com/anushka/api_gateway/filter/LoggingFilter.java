package com.anushka.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().name();
        Instant start = Instant.now();

        System.out.printf("[GATEWAY] Incoming request: %s %s%n", method, path);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
            System.out.printf("[GATEWAY] Completed: %s %s in %dms%n", method, path, duration);
        }));
    }

    @Override
    public int getOrder() {
        return -1; // run early, before routing
    }
}