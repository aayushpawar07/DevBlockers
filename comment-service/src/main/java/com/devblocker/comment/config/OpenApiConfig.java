package com.devblocker.comment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI commentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Comment Service API")
                        .description("Comment Management Service for DevBlocker - Threaded comments on blockers and solutions")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DevBlocker Team")
                                .email("support@devblocker.com")));
    }
}

