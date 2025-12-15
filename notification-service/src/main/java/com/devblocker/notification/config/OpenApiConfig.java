package com.devblocker.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("Notification Management Service for DevBlocker - In-app and email notifications")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DevBlocker Team")
                                .email("support@devblocker.com")));
    }
}

