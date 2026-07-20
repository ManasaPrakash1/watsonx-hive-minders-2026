package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI foodFiestaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Food Fiesta API")
                        .description("Comprehensive API documentation for Food Fiesta - Premium Dining Management System")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Food Fiesta Support")
                                .email("support@foodfiesta.com")));
    }
}
