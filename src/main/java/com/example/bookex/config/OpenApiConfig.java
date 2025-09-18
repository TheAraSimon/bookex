package com.example.bookex.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI appOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BookSwap API")
                        .description("API for listing books, swapping, ratings and user profiles.")
                        .version("v1"));
    }
}
