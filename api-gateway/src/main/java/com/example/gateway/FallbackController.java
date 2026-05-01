package com.example.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/greet-service")
    public Mono<ResponseEntity<Map<String, Object>>> greetFallback() {
        return fallback("greet-service");
    }

    @GetMapping("/hello-service")
    public Mono<ResponseEntity<Map<String, Object>>> helloFallback() {
        return fallback("hello-service");
    }

    private Mono<ResponseEntity<Map<String, Object>>> fallback(String service) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", service + " is temporarily unavailable. Please try again shortly."
                )));
    }
}
