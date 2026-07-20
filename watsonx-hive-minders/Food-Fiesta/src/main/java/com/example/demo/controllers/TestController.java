package com.example.demo.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Test API", description = "Simple health check to verify Swagger discovery")
public class TestController {

    @GetMapping("/api/health")
    @Operation(summary = "Health Check", description = "Verifies if the API discovery is working")
    public String health() {
        return "System is Online";
    }
}
