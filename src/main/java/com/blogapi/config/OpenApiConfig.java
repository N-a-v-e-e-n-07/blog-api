package com.blogapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.*;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Blog REST API",
                version = "1.0",
                description = "Full-featured blog backend with JWT auth, multi-entity relationships, nested comments, tags, and pagination",
                contact = @Contact(name = "Blog API Support", email = "support@blogapi.com")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Local Dev")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
