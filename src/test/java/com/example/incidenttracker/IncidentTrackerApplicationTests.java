package com.example.incidenttracker;

import com.example.incidenttracker.controller.IncidentController;
import com.example.incidenttracker.graphql.IncidentGraphQLController;
import com.example.incidenttracker.repository.IncidentRepository;
import com.example.incidenttracker.service.IncidentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Application context tests for IncidentTrackerApplication.
 * Verifies that the Spring Boot application starts correctly
 * and all components are properly wired.
 *
 * Uses @SpringBootTest for full application context testing.
 */
@SpringBootTest
@ActiveProfiles("test")
class IncidentTrackerApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IncidentController incidentController;

    @Autowired
    private IncidentGraphQLController graphQLController;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private IncidentRepository incidentRepository;

    @Test
    void testApplicationContextLoads() {
        // Assert
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void testControllerBeansExist() {
        // Assert
        assertThat(incidentController).isNotNull();
        assertThat(graphQLController).isNotNull();
    }

    @Test
    void testServiceBeanExists() {
        // Assert
        assertThat(incidentService).isNotNull();
    }

    @Test
    void testRepositoryBeanExists() {
        // Assert
        assertThat(incidentRepository).isNotNull();
    }

    @Test
    void testAllBeansLoaded() {
        // Verify key beans are registered
        assertThat(applicationContext.containsBean("incidentController")).isTrue();
        assertThat(applicationContext.containsBean("incidentGraphQLController")).isTrue();
        assertThat(applicationContext.containsBean("incidentServiceImpl")).isTrue();
        assertThat(applicationContext.containsBean("incidentRepository")).isTrue();
    }

    @Test
    void testApplicationPropertiesLoaded() {
        // Verify application properties are accessible
        String appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        assertThat(appName).isEqualTo("incident-tracker");
    }

    @Test
    void testApplicationStartsWithTestProfile() {
        // Verify test profile is active
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        assertThat(activeProfiles).contains("test");
    }

    @Test
    void testDatabaseConfigured() {
        // Verify H2 database is configured for tests
        String dbUrl = applicationContext.getEnvironment().getProperty("spring.datasource.url");
        assertThat(dbUrl).contains("h2:mem");
    }

    @Test
    void testHibernateDialectConfigured() {
        // Verify Hibernate dialect (H2 for test profile)
        String dialect = applicationContext.getEnvironment().getProperty("spring.jpa.database-platform");
        assertThat(dialect).contains("H2Dialect");
    }
}
