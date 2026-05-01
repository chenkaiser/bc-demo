package com.example.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = Optional
                .ofNullable(exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER))
                .orElseGet(() -> UUID.randomUUID().toString());

        // Echo back to the client so it can correlate its own logs
        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        // Propagate to every downstream service on this request
        return chain.filter(
                exchange.mutate()
                        .request(r -> r.header(REQUEST_ID_HEADER, requestId))
                        .build()
        );
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
