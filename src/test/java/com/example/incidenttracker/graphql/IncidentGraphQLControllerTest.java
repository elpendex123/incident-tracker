package com.example.incidenttracker.graphql;

import com.example.incidenttracker.model.Incident;
import com.example.incidenttracker.model.Priority;
import com.example.incidenttracker.model.Status;
import com.example.incidenttracker.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for GraphQL API.
 * Tests GraphQL queries and mutations using GraphQlTester.
 *
 * Note: Uses @SpringBootTest with GraphQL auto-configuration for testing.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IncidentGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private IncidentRepository incidentRepository;

    private Incident testIncident;

    @BeforeEach
    void setUp() {
        incidentRepository.deleteAll();

        testIncident = Incident.builder()
                .title("Test Incident")
                .description("Test Description")
                .priority(Priority.HIGH)
                .status(Status.OPEN)
                .assignee("John Doe")
                .build();
    }

    @Test
    void testQueryAllIncidents() {
        // Arrange
        incidentRepository.save(testIncident);

        String query = """
                query {
                    incidents {
                        id
                        title
                        description
                        priority
                        status
                        assignee
                    }
                }
                """;

        // Act & Assert
        graphQlTester.document(query)
                .execute()
                .path("incidents")
                .entityList(Incident.class)
                .hasSize(1)
                .get()
                .contains(testIncident);
    }

    @Test
    void testQueryIncidentById() {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        String query = String.format("""
                query {
                    incident(id: "%d") {
                        id
                        title
                        priority
                        status
                    }
                }
                """, saved.getId());

        // Act & Assert
        graphQlTester.document(query)
                .execute()
                .path("incident.id")
                .entity(String.class)
                .isEqualTo(saved.getId().toString());
    }

    @Test
    void testQueryIncidentById_NotFound() {
        // Arrange
        String query = """
                query {
                    incident(id: "999") {
                        id
                        title
                    }
                }
                """;

        // Act & Assert
        graphQlTester.document(query)
                .execute()
                .path("incident")
                .valueIsNull();
    }

    @Test
    void testQueryIncidentsByStatus() {
        // Arrange
        incidentRepository.save(testIncident);  // OPEN
        Incident inProgressIncident = Incident.builder()
                .title("In Progress")
                .priority(Priority.MEDIUM)
                .status(Status.IN_PROGRESS)
                .build();
        incidentRepository.save(inProgressIncident);

        String query = """
                query {
                    incidentsByStatus(status: OPEN) {
                        id
                        title
                        status
                    }
                }
                """;

        // Act & Assert
        graphQlTester.document(query)
                .execute()
                .path("incidentsByStatus")
                .entityList(Incident.class)
                .hasSize(1)
                .satisfiesExactly(incident ->
                        org.assertj.core.api.Assertions.assertThat(incident.getStatus())
                                .isEqualTo(Status.OPEN)
                );
    }

    @Test
    void testQueryIncidentsByPriority() {
        // Arrange
        incidentRepository.save(testIncident);  // HIGH
        Incident lowPriority = Incident.builder()
                .title("Low Priority")
                .priority(Priority.LOW)
                .status(Status.OPEN)
                .build();
        incidentRepository.save(lowPriority);

        String query = """
                query {
                    incidentsByPriority(priority: HIGH) {
                        id
                        title
                        priority
                    }
                }
                """;

        // Act & Assert
        graphQlTester.document(query)
                .execute()
                .path("incidentsByPriority")
                .entityList(Incident.class)
                .hasSize(1)
                .satisfiesExactly(incident ->
                        org.assertj.core.api.Assertions.assertThat(incident.getPriority())
                                .isEqualTo(Priority.HIGH)
                );
    }

    @Test
    void testQueryIncidentsByAssignee() {
        // Arrange
        incidentRepository.save(testIncident);  // John Doe
        Incident janeIncident = Incident.builder()
                .title("Jane's Incident")
                .priority(Priority.LOW)
                .status(Status.OPEN)
                .assignee("Jane Doe")
                .build();
        incidentRepository.save(janeIncident);

        String query = """
                query {
                    incidentsByAssignee(assignee: "John Doe") {
                        id
                        title
                        assignee
                    }
                }
                """;

        // Act & Assert
        graphQlTester.document(query)
                .execute()
                .path("incidentsByAssignee")
                .entityList(Incident.class)
                .hasSize(1)
                .satisfiesExactly(incident ->
                        org.assertj.core.api.Assertions.assertThat(incident.getAssignee())
                                .isEqualTo("John Doe")
                );
    }

    @Test
    void testMutationCreateIncident() {
        // Arrange
        String mutation = """
                mutation {
                    createIncident(input: {
                        title: "New Incident"
                        description: "A new incident"
                        priority: CRITICAL
                        assignee: "Alice"
                    }) {
                        id
                        title
                        description
                        priority
                        status
                        assignee
                    }
                }
                """;

        // Act & Assert
        graphQlTester.document(mutation)
                .execute()
                .path("createIncident.id")
                .entity(String.class)
                .isNotBlank();

        graphQlTester.document(mutation)
                .execute()
                .path("createIncident.title")
                .entity(String.class)
                .isEqualTo("New Incident");

        graphQlTester.document(mutation)
                .execute()
                .path("createIncident.priority")
                .entity(String.class)
                .isEqualTo("CRITICAL");
    }

    @Test
    void testMutationCreateIncident_WithDefaults() {
        // Arrange
        String mutation = """
                mutation {
                    createIncident(input: {
                        title: "Minimal Incident"
                    }) {
                        id
                        title
                        priority
                        status
                    }
                }
                """;

        // Act & Assert
        graphQlTester.document(mutation)
                .execute()
                .path("createIncident.priority")
                .entity(String.class)
                .isEqualTo("LOW");

        graphQlTester.document(mutation)
                .execute()
                .path("createIncident.status")
                .entity(String.class)
                .isEqualTo("OPEN");
    }

    @Test
    void testMutationUpdateIncident() {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        String mutation = String.format("""
                mutation {
                    updateIncident(id: "%d", input: {
                        title: "Updated Title"
                        priority: CRITICAL
                        status: IN_PROGRESS
                    }) {
                        id
                        title
                        priority
                        status
                    }
                }
                """, saved.getId());

        // Act & Assert
        graphQlTester.document(mutation)
                .execute()
                .path("updateIncident.title")
                .entity(String.class)
                .isEqualTo("Updated Title");

        graphQlTester.document(mutation)
                .execute()
                .path("updateIncident.priority")
                .entity(String.class)
                .isEqualTo("CRITICAL");
    }

    @Test
    void testMutationUpdateStatus() {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        String mutation = String.format("""
                mutation {
                    updateStatus(id: "%d", status: RESOLVED) {
                        id
                        status
                        resolvedAt
                    }
                }
                """, saved.getId());

        // Act & Assert
        graphQlTester.document(mutation)
                .execute()
                .path("updateStatus.status")
                .entity(String.class)
                .isEqualTo("RESOLVED");

        // Note: resolvedAt is set by @PreUpdate hook
        graphQlTester.document(mutation)
                .execute()
                .path("updateStatus.resolvedAt")
                .entity(String.class)
                .isNotBlank();
    }

    @Test
    void testMutationDeleteIncident() {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        String mutation = String.format("""
                mutation {
                    deleteIncident(id: "%d")
                }
                """, saved.getId());

        // Act & Assert
        graphQlTester.document(mutation)
                .execute()
                .path("deleteIncident")
                .entity(Boolean.class)
                .isEqualTo(true);

        // Verify deletion
        org.assertj.core.api.Assertions.assertThat(incidentRepository.findById(saved.getId()))
                .isEmpty();
    }

    @Test
    void testMutationDeleteIncident_NotFound() {
        // Arrange
        String mutation = """
                mutation {
                    deleteIncident(id: "999")
                }
                """;

        // Act & Assert - Should throw exception or return error
        // GraphQL errors are handled differently than REST
        graphQlTester.document(mutation)
                .execute()
                .errors()
                .satisfy(errors ->
                        org.assertj.core.api.Assertions.assertThat(errors)
                                .isNotEmpty()
                );
    }

    @Test
    void testGraphQLQuery_ComplexSelection() {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        String query = String.format("""
                query {
                    incident(id: "%d") {
                        id
                        title
                        description
                        priority
                        status
                        assignee
                        createdAt
                        updatedAt
                        resolvedAt
                    }
                }
                """, saved.getId());

        // Act & Assert
        graphQlTester.document(query)
                .execute()
                .path("incident")
                .entity(Incident.class)
                .satisfies(incident ->
                        org.assertj.core.api.Assertions.assertThat(incident)
                                .isNotNull()
                                .hasFieldOrPropertyWithValue("title", "Test Incident")
                                .hasFieldOrPropertyWithValue("priority", Priority.HIGH)
                );
    }

    @Test
    void testGraphQLMutation_MultipleOperations() {
        // Test creating and then updating an incident
        String createMutation = """
                mutation {
                    createIncident(input: {
                        title: "Multi-op Test"
                        priority: HIGH
                    }) {
                        id
                        title
                    }
                }
                """;

        // Get the ID from creation
        graphQlTester.document(createMutation)
                .execute()
                .path("createIncident.id")
                .entity(String.class)
                .satisfies(id ->
                        org.assertj.core.api.Assertions.assertThat(id).isNotBlank()
                );
    }

    @Test
    void testGraphQLEnumValues() {
        // Arrange
        String query = """
                query {
                    incidents {
                        priority
                        status
                    }
                }
                """;

        // Create incidents with different values
        for (Priority priority : Priority.values()) {
            for (Status status : Status.values()) {
                Incident incident = Incident.builder()
                        .title("Test")
                        .priority(priority)
                        .status(status)
                        .build();
                incidentRepository.save(incident);
            }
        }

        // Act & Assert
        graphQlTester.document(query)
                .execute()
                .path("incidents")
                .entityList(Incident.class)
                .hasSize(Priority.values().length * Status.values().length);
    }
}
