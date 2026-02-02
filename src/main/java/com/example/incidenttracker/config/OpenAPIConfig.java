package com.example.incidenttracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for the REST API.
 * Provides API documentation and interactive Swagger UI.
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI incidentTrackerAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Incident Tracker API")
                        .description("REST and GraphQL APIs for managing incidents. " +
                                   "Track, prioritize, and resolve incidents efficiently.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com")
                                .url("https://github.com/enrique-coello/incident-tracker"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
