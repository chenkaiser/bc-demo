package com.example.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void echoesExistingRequestId() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header(RequestIdFilter.REQUEST_ID_HEADER, "my-id-123")
                        .build());

        filter.filter(exchange, chain -> Mono.empty()).block();

        assertThat(exchange.getResponse().getHeaders().getFirst(RequestIdFilter.REQUEST_ID_HEADER))
                .isEqualTo("my-id-123");
    }

    @Test
    void generatesRequestIdWhenMissing() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build());

        filter.filter(exchange, chain -> Mono.empty()).block();

        String generated = exchange.getResponse().getHeaders().getFirst(RequestIdFilter.REQUEST_ID_HEADER);
        assertThat(generated).isNotBlank();
    }

    @Test
    void propagatesRequestIdToDownstreamRequest() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header(RequestIdFilter.REQUEST_ID_HEADER, "downstream-id")
                        .build());

        filter.filter(exchange, mutated -> {
            assertThat(mutated.getRequest().getHeaders()
                    .getFirst(RequestIdFilter.REQUEST_ID_HEADER))
                    .isEqualTo("downstream-id");
            return Mono.empty();
        }).block();
    }

    @Test
    void returnsHighestPrecedenceOrder() {
        assertThat(filter.getOrder()).isEqualTo(Integer.MIN_VALUE);
    }
}
