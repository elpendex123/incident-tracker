package com.example.incidenttracker.graphql;

import com.example.incidenttracker.model.Incident;
import com.example.incidenttracker.model.Priority;
import com.example.incidenttracker.model.Status;
import com.example.incidenttracker.repository.IncidentRepository;
import com.example.incidenttracker.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for GraphQL Controller.
 * Tests GraphQL resolver methods by directly calling controller methods.
 *
 * Note: Verifies that GraphQL resolvers correctly delegate to service layer.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IncidentGraphQLControllerTest {

    @Autowired
    private IncidentGraphQLController graphQLController;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentService incidentService;

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
        Incident saved = incidentRepository.save(testIncident);

        // Act
        List<Incident> incidents = graphQLController.incidents();

        // Assert
        assertThat(incidents).hasSize(1);
        assertThat(incidents.get(0).getTitle()).isEqualTo("Test Incident");
    }

    @Test
    void testQueryIncidentById() {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        // Act
        Incident result = graphQLController.incident(saved.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getTitle()).isEqualTo("Test Incident");
    }

    @Test
    void testQueryIncidentById_NotFound() {
        // Act & Assert
        assertThatThrownBy(() -> graphQLController.incident(999L))
                .isInstanceOf(com.example.incidenttracker.exception.ResourceNotFoundException.class);
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

        // Act
        List<Incident> results = graphQLController.incidentsByStatus(Status.OPEN);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results).allMatch(i -> i.getStatus() == Status.OPEN);
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

        // Act
        List<Incident> results = graphQLController.incidentsByPriority(Priority.HIGH);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results).allMatch(i -> i.getPriority() == Priority.HIGH);
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

        // Act
        List<Incident> results = graphQLController.incidentsByAssignee("John Doe");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results).allMatch(i -> "John Doe".equals(i.getAssignee()));
    }

    @Test
    void testMutationCreateIncident() {
        // Arrange
        IncidentGraphQLController.CreateIncidentInput input = new IncidentGraphQLController.CreateIncidentInput(
                "New Incident",
                "A new incident",
                Priority.CRITICAL,
                "Alice"
        );

        // Act
        Incident result = graphQLController.createIncident(input);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Incident");
        assertThat(result.getPriority()).isEqualTo(Priority.CRITICAL);
        assertThat(result.getStatus()).isEqualTo(Status.OPEN);
    }

    @Test
    void testMutationCreateIncident_WithDefaults() {
        // Arrange
        IncidentGraphQLController.CreateIncidentInput input = new IncidentGraphQLController.CreateIncidentInput(
                "Minimal Incident",
                null,
                null,
                null
        );

        // Act
        Incident result = graphQLController.createIncident(input);

        // Assert
        assertThat(result.getTitle()).isEqualTo("Minimal Incident");
        assertThat(result.getPriority()).isEqualTo(Priority.LOW);
        assertThat(result.getStatus()).isEqualTo(Status.OPEN);
    }

    @Test
    void testMutationUpdateIncident() {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);
        IncidentGraphQLController.UpdateIncidentInput input = new IncidentGraphQLController.UpdateIncidentInput(
                "Updated Title",
                "Updated Description",
                Priority.CRITICAL,
                Status.IN_PROGRESS,
                "Bob"
        );

        // Act
        Incident result = graphQLController.updateIncident(saved.getId(), input);

        // Assert
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getPriority()).isEqualTo(Priority.CRITICAL);
        assertThat(result.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void testMutationUpdateStatus() {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        // Act
        Incident result = graphQLController.updateStatus(saved.getId(), Status.RESOLVED);

        // Assert
        assertThat(result.getStatus()).isEqualTo(Status.RESOLVED);
        assertThat(result.getResolvedAt()).isNotNull();
    }

    @Test
    void testMutationDeleteIncident() {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        // Act
        Boolean result = graphQLController.deleteIncident(saved.getId());

        // Assert
        assertThat(result).isTrue();
        assertThat(incidentRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void testMutationDeleteIncident_NotFound() {
        // Act & Assert
        assertThatThrownBy(() -> graphQLController.deleteIncident(999L))
                .isInstanceOf(com.example.incidenttracker.exception.ResourceNotFoundException.class);
    }

    @Test
    void testGraphQLEnumValues() {
        // Arrange & Act - Create incidents with different priority and status values
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

        // Act
        List<Incident> all = graphQLController.incidents();

        // Assert
        assertThat(all).hasSize(Priority.values().length * Status.values().length);
    }

    @Test
    void testGraphQLMutation_MultipleOperations() {
        // Arrange - Create incident
        IncidentGraphQLController.CreateIncidentInput createInput = new IncidentGraphQLController.CreateIncidentInput(
                "Multi-op Test",
                null,
                Priority.HIGH,
                null
        );

        // Act - Create
        Incident created = graphQLController.createIncident(createInput);

        // Assert created
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Multi-op Test");
        assertThat(created.getPriority()).isEqualTo(Priority.HIGH);

        // Act - Update status
        Incident updated = graphQLController.updateStatus(created.getId(), Status.RESOLVED);

        // Assert updated
        assertThat(updated.getStatus()).isEqualTo(Status.RESOLVED);
        assertThat(updated.getResolvedAt()).isNotNull();
    }

    @Test
    void testGraphQLQuery_ComplexSelection() {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        // Act
        Incident result = graphQLController.incident(saved.getId());

        // Assert - Verify all fields are populated
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getTitle()).isEqualTo("Test Incident");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(result.getStatus()).isEqualTo(Status.OPEN);
        assertThat(result.getAssignee()).isEqualTo("John Doe");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }
}
