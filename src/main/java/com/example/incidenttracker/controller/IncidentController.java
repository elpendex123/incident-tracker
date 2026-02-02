package com.example.incidenttracker.controller;

import com.example.incidenttracker.dto.IncidentRequest;
import com.example.incidenttracker.dto.IncidentResponse;
import com.example.incidenttracker.model.Incident;
import com.example.incidenttracker.model.Priority;
import com.example.incidenttracker.model.Status;
import com.example.incidenttracker.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API controller for incident management.
 * Provides endpoints for CRUD operations on incidents.
 * All responses are documented with OpenAPI annotations.
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Incident Management", description = "APIs for managing incidents")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @Operation(summary = "Get all incidents",
               description = "Retrieve all incidents with optional filters by status or priority")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of incidents"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameter")
    })
    public ResponseEntity<List<IncidentResponse>> getAllIncidents(
            @RequestParam(required = false)
            @Parameter(description = "Filter by incident status")
            Status status,

            @RequestParam(required = false)
            @Parameter(description = "Filter by incident priority")
            Priority priority) {

        log.debug("GET /api/incidents - status={}, priority={}", status, priority);

        List<Incident> incidents;

        if (status != null && priority != null) {
            incidents = incidentService.getAllIncidents().stream()
                    .filter(i -> i.getStatus() == status && i.getPriority() == priority)
                    .collect(Collectors.toList());
        } else if (status != null) {
            incidents = incidentService.getIncidentsByStatus(status);
        } else if (priority != null) {
            incidents = incidentService.getIncidentsByPriority(priority);
        } else {
            incidents = incidentService.getAllIncidents();
        }

        List<IncidentResponse> responses = incidents.stream()
                .map(IncidentResponse::fromEntity)
                .collect(Collectors.toList());

        log.debug("Returning {} incidents", responses.size());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID",
               description = "Retrieve a specific incident by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved incident"),
        @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponse> getIncidentById(
            @PathVariable
            @Parameter(description = "Incident ID")
            Long id) {

        log.debug("GET /api/incidents/{}", id);
        Incident incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(IncidentResponse.fromEntity(incident));
    }

    @PostMapping
    @Operation(summary = "Create new incident",
               description = "Create a new incident with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Incident successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<IncidentResponse> createIncident(
            @Valid @RequestBody
            IncidentRequest request) {

        log.debug("POST /api/incidents - title={}", request.getTitle());
        Incident incident = incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(IncidentResponse.fromEntity(incident));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update incident",
               description = "Update all fields of an existing incident")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Incident successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponse> updateIncident(
            @PathVariable
            @Parameter(description = "Incident ID")
            Long id,

            @Valid @RequestBody
            IncidentRequest request) {

        log.debug("PUT /api/incidents/{}", id);
        Incident incident = incidentService.updateIncident(id, request);
        return ResponseEntity.ok(IncidentResponse.fromEntity(incident));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update incident status only",
               description = "Update only the status of an incident. " +
                           "When set to RESOLVED, resolvedAt timestamp is automatically recorded.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status successfully updated"),
        @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponse> updateStatus(
            @PathVariable
            @Parameter(description = "Incident ID")
            Long id,

            @RequestBody
            @Parameter(description = "New incident status")
            Status status) {

        log.debug("PATCH /api/incidents/{}/status - status={}", id, status);
        Incident incident = incidentService.updateStatus(id, status);
        return ResponseEntity.ok(IncidentResponse.fromEntity(incident));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete incident",
               description = "Permanently delete an incident")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Incident successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<Void> deleteIncident(
            @PathVariable
            @Parameter(description = "Incident ID")
            Long id) {

        log.debug("DELETE /api/incidents/{}", id);
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }
}
