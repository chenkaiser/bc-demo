package com.example.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalGatewayErrorHandlerTest {

    private final GlobalGatewayErrorHandler handler =
            new GlobalGatewayErrorHandler(new ObjectMapper());

    @Test
    void returns404ForNoRouteResponseStatusException() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/missing").build());
        var ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "No route");

        handler.handle(exchange, ex).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void returns500ForGenericException() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/boom").build());

        handler.handle(exchange, new RuntimeException("unexpected")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void responseBodyContainsExpectedFields() throws Exception {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        var ex = new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");

        handler.handle(exchange, ex).block();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = new ObjectMapper().readValue(
                exchange.getResponse().getBodyAsString().block(), Map.class);

        assertThat(body).containsKeys("timestamp", "status", "error", "message", "path");
        assertThat(body.get("status")).isEqualTo(429);
        assertThat(body.get("path")).isEqualTo("/test");
    }

    @Test
    void hidesInternalMessageFor5xxErrors() throws Exception {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

        handler.handle(exchange, new RuntimeException("secret db password")).block();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = new ObjectMapper().readValue(
                exchange.getResponse().getBodyAsString().block(), Map.class);

        assertThat(body.get("message")).isEqualTo("An unexpected error occurred");
    }
}
