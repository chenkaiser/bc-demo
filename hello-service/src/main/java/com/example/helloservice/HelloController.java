package com.example.helloservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Value("${server.port}")
    private int port;

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            host = "unknown";
        }
        return ResponseEntity.ok(Map.of(
                "message", "Hello World",
                "instance", host + ":" + port
        ));
    }
}
