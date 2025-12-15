package com.devblocker.solution.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI solutionServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Solution Service API")
                        .description("Solution Management Service for DevBlocker - Add solutions to blockers, upvote, and accept best solutions")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DevBlocker Team")
                                .email("support@devblocker.com")));
    }
}

