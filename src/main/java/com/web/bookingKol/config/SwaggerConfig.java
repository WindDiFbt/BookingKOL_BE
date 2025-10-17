package com.web.bookingKol.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        Server ngrokServer = new Server()
                .url("https://untumid-disposingly-santos.ngrok-free.dev/api/v1")
                .description("Ngrok/Production Server");
        Server localHost = new Server()
                .url("http://localhost:8080/api/v1")
                .description("Ngrok/Production Server");
        Server testServer = new Server()
                .url("http://54.179.248.120/api/v1")
                .description("Test Server");
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                ).servers(List.of(ngrokServer, localHost, testServer));
    }
}

