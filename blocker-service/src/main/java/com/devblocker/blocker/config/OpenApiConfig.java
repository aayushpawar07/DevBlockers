package com.devblocker.blocker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blocker Service API")
                        .version("1.0.0")
                        .description("Blocker Management Service API - Create, update, resolve blockers with tagging and assignment")
                        .contact(new Contact()
                                .name("DevBlocker")
                                .email("support@devblocker.com")));
    }
}

