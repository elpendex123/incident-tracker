package com.example.incidenttracker.graphql;

import com.example.incidenttracker.dto.IncidentRequest;
import com.example.incidenttracker.model.Incident;
import com.example.incidenttracker.model.Priority;
import com.example.incidenttracker.model.Status;
import com.example.incidenttracker.service.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL controller for incident queries and mutations.
 * Provides flexible GraphQL interface for incident management.
 *
 * Note: Uses @Controller (not @RestController) for GraphQL endpoints.
 * All resolvers are method-level using @QueryMapping and @MutationMapping.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class IncidentGraphQLController {

    private final IncidentService incidentService;

    // ============ QUERIES ============

    @QueryMapping
    public List<Incident> incidents() {
        log.debug("GraphQL query: incidents()");
        return incidentService.getAllIncidents();
    }

    @QueryMapping
    public Incident incident(@Argument Long id) {
        log.debug("GraphQL query: incident(id={})", id);
        return incidentService.getIncidentById(id);
    }

    @QueryMapping
    public List<Incident> incidentsByStatus(@Argument Status status) {
        log.debug("GraphQL query: incidentsByStatus(status={})", status);
        return incidentService.getIncidentsByStatus(status);
    }

    @QueryMapping
    public List<Incident> incidentsByPriority(@Argument Priority priority) {
        log.debug("GraphQL query: incidentsByPriority(priority={})", priority);
        return incidentService.getIncidentsByPriority(priority);
    }

    @QueryMapping
    public List<Incident> incidentsByAssignee(@Argument String assignee) {
        log.debug("GraphQL query: incidentsByAssignee(assignee={})", assignee);
        return incidentService.getIncidentsByAssignee(assignee);
    }

    // ============ MUTATIONS ============

    @MutationMapping
    public Incident createIncident(@Argument CreateIncidentInput input) {
        log.info("GraphQL mutation: createIncident(title={})", input.title());

        IncidentRequest request = IncidentRequest.builder()
                .title(input.title())
                .description(input.description())
                .priority(input.priority() != null ? input.priority() : Priority.LOW)
                .assignee(input.assignee())
                .build();

        return incidentService.createIncident(request);
    }

    @MutationMapping
    public Incident updateIncident(@Argument Long id, @Argument UpdateIncidentInput input) {
        log.info("GraphQL mutation: updateIncident(id={})", id);

        IncidentRequest request = IncidentRequest.builder()
                .title(input.title())
                .description(input.description())
                .priority(input.priority())
                .status(input.status())
                .assignee(input.assignee())
                .build();

        return incidentService.updateIncident(id, request);
    }

    @MutationMapping
    public Incident updateStatus(@Argument Long id, @Argument Status status) {
        log.info("GraphQL mutation: updateStatus(id={}, status={})", id, status);
        return incidentService.updateStatus(id, status);
    }

    @MutationMapping
    public Boolean deleteIncident(@Argument Long id) {
        log.info("GraphQL mutation: deleteIncident(id={})", id);
        incidentService.deleteIncident(id);
        return true;
    }

    // ============ INPUT TYPES ============

    /**
     * GraphQL input type for creating incidents.
     * Uses Java 17 record classes for concise definition.
     */
    public record CreateIncidentInput(
            String title,
            String description,
            Priority priority,
            String assignee
    ) {}

    /**
     * GraphQL input type for updating incidents.
     * Uses Java 17 record classes for concise definition.
     */
    public record UpdateIncidentInput(
            String title,
            String description,
            Priority priority,
            Status status,
            String assignee
    ) {}
}
