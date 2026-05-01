package com.example.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

@WebFluxTest(
        controllers = FallbackController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class,
                ReactiveOAuth2ResourceServerAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class
        )
)
class FallbackControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void greetFallbackReturns503WithStructuredBody() {
        webTestClient.get().uri("/fallback/greet-service")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.timestamp").value(notNullValue())
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service Unavailable")
                .jsonPath("$.message").value(containsString("greet-service"));
    }

    @Test
    void helloFallbackReturns503WithStructuredBody() {
        webTestClient.get().uri("/fallback/hello-service")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.timestamp").value(notNullValue())
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service Unavailable")
                .jsonPath("$.message").value(containsString("hello-service"));
    }
}
