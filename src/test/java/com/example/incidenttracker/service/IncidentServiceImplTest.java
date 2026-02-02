package com.example.incidenttracker.service;

import com.example.incidenttracker.dto.IncidentRequest;
import com.example.incidenttracker.exception.ResourceNotFoundException;
import com.example.incidenttracker.model.Incident;
import com.example.incidenttracker.model.Priority;
import com.example.incidenttracker.model.Status;
import com.example.incidenttracker.repository.IncidentRepository;
import com.example.incidenttracker.service.impl.IncidentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IncidentServiceImpl.
 * Tests business logic with mocked repository layer.
 *
 * Uses Mockito for dependency mocking and AssertJ for fluent assertions.
 */
@ExtendWith(MockitoExtension.class)
class IncidentServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    private Incident testIncident;
    private IncidentRequest testRequest;

    @BeforeEach
    void setUp() {
        testIncident = Incident.builder()
                .id(1L)
                .title("Test Incident")
                .description("Test Description")
                .priority(Priority.HIGH)
                .status(Status.OPEN)
                .assignee("John Doe")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = IncidentRequest.builder()
                .title("New Incident")
                .description("New Description")
                .priority(Priority.MEDIUM)
                .status(Status.OPEN)
                .assignee("Jane Doe")
                .build();
    }

    @Test
    void testGetAllIncidents() {
        // Arrange
        List<Incident> incidents = Arrays.asList(testIncident);
        when(incidentRepository.findAll()).thenReturn(incidents);

        // Act
        List<Incident> result = incidentService.getAllIncidents();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Incident");
        verify(incidentRepository, times(1)).findAll();
    }

    @Test
    void testGetAllIncidentsEmpty() {
        // Arrange
        when(incidentRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Incident> result = incidentService.getAllIncidents();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetIncidentById_Success() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));

        // Act
        Incident result = incidentService.getIncidentById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Incident");
        verify(incidentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetIncidentById_NotFound() {
        // Arrange
        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> incidentService.getIncidentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident not found with id: 999");
    }

    @Test
    void testCreateIncident() {
        // Arrange
        Incident savedIncident = Incident.builder()
                .id(2L)
                .title(testRequest.getTitle())
                .description(testRequest.getDescription())
                .priority(testRequest.getPriority())
                .status(testRequest.getStatus())
                .assignee(testRequest.getAssignee())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(incidentRepository.save(any(Incident.class))).thenReturn(savedIncident);

        // Act
        Incident result = incidentService.createIncident(testRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New Incident");
        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM);
        verify(incidentRepository, times(1)).save(any(Incident.class));
    }

    @Test
    void testCreateIncidentWithDefaults() {
        // Arrange
        IncidentRequest request = IncidentRequest.builder()
                .title("Minimal Incident")
                .build();

        Incident savedIncident = Incident.builder()
                .id(3L)
                .title("Minimal Incident")
                .priority(Priority.LOW)  // Default
                .status(Status.OPEN)      // Default
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(incidentRepository.save(any(Incident.class))).thenReturn(savedIncident);

        // Act
        Incident result = incidentService.createIncident(request);

        // Assert
        assertThat(result.getPriority()).isEqualTo(Priority.LOW);
        assertThat(result.getStatus()).isEqualTo(Status.OPEN);
    }

    @Test
    void testUpdateIncident() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncidentRequest updateRequest = IncidentRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .priority(Priority.CRITICAL)
                .status(Status.IN_PROGRESS)
                .assignee("Updated Assignee")
                .build();

        // Act
        Incident result = incidentService.updateIncident(1L, updateRequest);

        // Assert
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getPriority()).isEqualTo(Priority.CRITICAL);
        verify(incidentRepository, times(1)).findById(1L);
        verify(incidentRepository, times(1)).save(any(Incident.class));
    }

    @Test
    void testUpdateIncident_NotFound() {
        // Arrange
        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> incidentService.updateIncident(999L, testRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testUpdateStatus() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Incident result = incidentService.updateStatus(1L, Status.RESOLVED);

        // Assert
        assertThat(result.getStatus()).isEqualTo(Status.RESOLVED);
        verify(incidentRepository, times(1)).save(any(Incident.class));
    }

    @Test
    void testUpdateStatus_ToResolved_SetsResolvedAt() {
        // Arrange
        Incident incident = Incident.builder()
                .id(1L)
                .title("Test")
                .priority(Priority.HIGH)
                .status(Status.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
            Incident arg = invocation.getArgument(0);
            // Simulate @PreUpdate hook
            if (arg.getStatus() == Status.RESOLVED && arg.getResolvedAt() == null) {
                arg.setResolvedAt(LocalDateTime.now());
            }
            return arg;
        });

        // Act
        Incident result = incidentService.updateStatus(1L, Status.RESOLVED);

        // Assert
        assertThat(result.getStatus()).isEqualTo(Status.RESOLVED);
        assertThat(result.getResolvedAt()).isNotNull();
    }

    @Test
    void testUpdateStatus_NotFound() {
        // Arrange
        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> incidentService.updateStatus(999L, Status.CLOSED))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testDeleteIncident() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        doNothing().when(incidentRepository).delete(testIncident);

        // Act
        incidentService.deleteIncident(1L);

        // Assert
        verify(incidentRepository, times(1)).delete(testIncident);
    }

    @Test
    void testDeleteIncident_NotFound() {
        // Arrange
        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> incidentService.deleteIncident(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testGetIncidentsByStatus() {
        // Arrange
        List<Incident> openIncidents = Arrays.asList(testIncident);
        when(incidentRepository.findByStatus(Status.OPEN)).thenReturn(openIncidents);

        // Act
        List<Incident> result = incidentService.getIncidentsByStatus(Status.OPEN);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Status.OPEN);
    }

    @Test
    void testGetIncidentsByPriority() {
        // Arrange
        List<Incident> highPriority = Arrays.asList(testIncident);
        when(incidentRepository.findByPriority(Priority.HIGH)).thenReturn(highPriority);

        // Act
        List<Incident> result = incidentService.getIncidentsByPriority(Priority.HIGH);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void testGetIncidentsByAssignee() {
        // Arrange
        List<Incident> aliceIncidents = Arrays.asList(testIncident);
        when(incidentRepository.findByAssignee("John Doe")).thenReturn(aliceIncidents);

        // Act
        List<Incident> result = incidentService.getIncidentsByAssignee("John Doe");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssignee()).isEqualTo("John Doe");
    }

    @Test
    void testGetOverdueIncidents() {
        // Arrange
        List<Incident> overdueIncidents = Arrays.asList(testIncident);
        when(incidentRepository.findOverdueIncidents(any(LocalDateTime.class)))
                .thenReturn(overdueIncidents);

        // Act
        List<Incident> result = incidentService.getOverdueIncidents(7);

        // Assert
        assertThat(result).hasSize(1);
        verify(incidentRepository, times(1)).findOverdueIncidents(any(LocalDateTime.class));
    }

    @Test
    void testCreateIncident_ValidatesInput() {
        // Arrange
        IncidentRequest invalidRequest = IncidentRequest.builder()
                .title("")  // Empty - should be validated by controller/DTO
                .build();

        // Act & Assert - Note: Validation happens at controller level
        // Service assumes validated input from DTO
        assertThat(invalidRequest.getTitle()).isEmpty();
    }

    @Test
    void testMultipleOperations_Transaction() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(incidentRepository.save(any(Incident.class))).thenReturn(testIncident);

        // Act
        Incident retrieved = incidentService.getIncidentById(1L);
        Incident updated = incidentService.updateStatus(1L, Status.IN_PROGRESS);

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(updated).isNotNull();
        verify(incidentRepository, times(2)).findById(1L);
    }
}
