package com.example.homework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final AppSettingsProperties appSettings;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(appSettings.getTitle())
                        .version("1.0")
                        .description("RESTful API for managing students and their enrolled courses")
                        .contact(new Contact().email(appSettings.getContactEmail())));
    }
}
