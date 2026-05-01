package com.example.greetservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GreetController {

    @Value("${server.port}")
    private int port;

    @GetMapping("/greet/{username}")
    public ResponseEntity<Map<String, String>> greet(@PathVariable String username) {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            host = "unknown";
        }
        return ResponseEntity.ok(Map.of(
                "message", "greeting .... " + username,
                "instance", host + ":" + port
        ));
    }
}
