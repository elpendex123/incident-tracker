package com.example.incidenttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the Incident Tracker Spring Boot application.
 *
 * Provides REST and GraphQL APIs for incident management with:
 * - PostgreSQL persistence
 * - Automatic audit timestamps (createdAt, updatedAt)
 * - OpenAPI/Swagger documentation
 * - Comprehensive logging
 * - Exception handling
 * - Input validation
 */
@SpringBootApplication
@EnableJpaAuditing
public class IncidentTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentTrackerApplication.class, args);
    }
}
