package com.example.incidenttracker.controller;

import com.example.incidenttracker.dto.IncidentRequest;
import com.example.incidenttracker.model.Incident;
import com.example.incidenttracker.model.Priority;
import com.example.incidenttracker.model.Status;
import com.example.incidenttracker.repository.IncidentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for IncidentController REST API.
 * Tests REST endpoints with MockMvc using a real Spring context and in-memory H2 database.
 *
 * Uses @SpringBootTest with @AutoConfigureMockMvc for integration testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void testCreateIncident_WithValidData_ShouldReturn201() throws Exception {
        // Arrange
        IncidentRequest request = IncidentRequest.builder()
                .title("Database Connection Slow")
                .description("Queries are taking longer than usual")
                .priority(Priority.HIGH)
                .assignee("Alice")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Database Connection Slow"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testCreateIncident_WithInvalidData_BlankTitle_ShouldReturn400() throws Exception {
        // Arrange
        IncidentRequest request = IncidentRequest.builder()
                .title("") // Invalid - blank title
                .description("Description")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors", hasSize(greaterThan(0))));
    }

    @Test
    void testCreateIncident_WithInvalidData_TitleTooLong_ShouldReturn400() throws Exception {
        // Arrange
        StringBuilder longTitle = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            longTitle.append("a");
        }

        IncidentRequest request = IncidentRequest.builder()
                .title(longTitle.toString())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void testGetAllIncidents_ShouldReturnList() throws Exception {
        // Arrange
        incidentRepository.save(testIncident);

        // Act & Assert
        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title").value("Test Incident"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }

    @Test
    void testGetAllIncidents_Empty_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetAllIncidents_WithStatusFilter_ShouldReturnFiltered() throws Exception {
        // Arrange
        incidentRepository.save(testIncident);
        Incident resolvedIncident = Incident.builder()
                .title("Resolved Incident")
                .priority(Priority.LOW)
                .status(Status.RESOLVED)
                .build();
        incidentRepository.save(resolvedIncident);

        // Act & Assert
        mockMvc.perform(get("/api/incidents?status=OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void testGetAllIncidents_WithPriorityFilter_ShouldReturnFiltered() throws Exception {
        // Arrange
        incidentRepository.save(testIncident);  // HIGH
        Incident lowPriority = Incident.builder()
                .title("Low Priority")
                .priority(Priority.LOW)
                .status(Status.OPEN)
                .build();
        incidentRepository.save(lowPriority);

        // Act & Assert
        mockMvc.perform(get("/api/incidents?priority=HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }

    @Test
    void testGetAllIncidents_WithBothFilters_ShouldReturnFiltered() throws Exception {
        // Arrange
        incidentRepository.save(testIncident);  // HIGH + OPEN
        Incident criticalClosed = Incident.builder()
                .title("Closed Critical")
                .priority(Priority.CRITICAL)
                .status(Status.CLOSED)
                .build();
        incidentRepository.save(criticalClosed);

        // Act & Assert
        mockMvc.perform(get("/api/incidents?status=OPEN&priority=HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Incident"));
    }

    @Test
    void testGetIncidentById_WhenExists_ShouldReturn200() throws Exception {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        // Act & Assert
        mockMvc.perform(get("/api/incidents/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Test Incident"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void testGetIncidentById_WhenNotExists_ShouldReturn404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/incidents/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void testUpdateIncident_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        IncidentRequest updateRequest = IncidentRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .priority(Priority.CRITICAL)
                .status(Status.IN_PROGRESS)
                .assignee("Bob")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/incidents/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void testUpdateIncident_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        IncidentRequest updateRequest = IncidentRequest.builder()
                .title("Updated")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/incidents/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateStatus_ToResolved_ShouldReturn200() throws Exception {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        // Act & Assert
        mockMvc.perform(patch("/api/incidents/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"RESOLVED\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedAt").exists());
    }

    @Test
    void testUpdateStatus_WhenNotExists_ShouldReturn404() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/incidents/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"CLOSED\""))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteIncident_WhenExists_ShouldReturn204() throws Exception {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        // Act & Assert
        mockMvc.perform(delete("/api/incidents/" + saved.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/incidents/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteIncident_WhenNotExists_ShouldReturn404() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/incidents/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateStatus_AllStatuses_ShouldSucceed() throws Exception {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        // Test all status transitions
        Status[] statuses = {Status.IN_PROGRESS, Status.RESOLVED, Status.CLOSED};

        for (Status status : statuses) {
            // Act & Assert
            mockMvc.perform(patch("/api/incidents/" + saved.getId() + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("\"" + status + "\""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(status.toString()));
        }
    }

    @Test
    void testCreateMultipleIncidents_ShouldPersistAll() throws Exception {
        // Arrange
        for (int i = 1; i <= 3; i++) {
            IncidentRequest request = IncidentRequest.builder()
                    .title("Incident " + i)
                    .priority(Priority.values()[i % Priority.values().length])
                    .build();

            mockMvc.perform(post("/api/incidents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Act & Assert
        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void testContentNegotiation_ReturnsJSON() throws Exception {
        // Arrange
        incidentRepository.save(testIncident);

        // Act & Assert
        mockMvc.perform(get("/api/incidents")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testIncidentResponse_ContainsAllFields() throws Exception {
        // Arrange
        Incident saved = incidentRepository.save(testIncident);

        // Act & Assert
        mockMvc.perform(get("/api/incidents/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.priority").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.assignee").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
        // Note: resolvedAt is optional and only set when status is RESOLVED
    }
}
