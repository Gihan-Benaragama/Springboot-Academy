package com.springbootacademy.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI academyOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Springboot Academy Course Management API")
                .description("REST API for managing students, courses and enrollments at Springboot Academy.")
                .version("1.0.0")
                .contact(new Contact().name("Springboot Academy")));
    }
}
