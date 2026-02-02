package com.example.incidenttracker.service.impl;

import com.example.incidenttracker.dto.IncidentRequest;
import com.example.incidenttracker.exception.ResourceNotFoundException;
import com.example.incidenttracker.model.Incident;
import com.example.incidenttracker.model.Priority;
import com.example.incidenttracker.model.Status;
import com.example.incidenttracker.repository.IncidentRepository;
import com.example.incidenttracker.service.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of IncidentService.
 * Provides business logic for incident management with proper transaction handling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;

    @Override
    public List<Incident> getAllIncidents() {
        log.debug("Fetching all incidents");
        return incidentRepository.findAll();
    }

    @Override
    public Incident getIncidentById(Long id) {
        log.debug("Fetching incident with id: {}", id);
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Incident not found with id: " + id));
    }

    @Override
    @Transactional
    public Incident createIncident(IncidentRequest request) {
        log.info("Creating new incident: {}", request.getTitle());

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.LOW)
                .status(request.getStatus() != null ? request.getStatus() : Status.OPEN)
                .assignee(request.getAssignee())
                .build();

        Incident saved = incidentRepository.save(incident);
        log.info("Created incident with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Incident updateIncident(Long id, IncidentRequest request) {
        log.info("Updating incident with id: {}", id);

        Incident incident = getIncidentById(id);

        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());
        incident.setPriority(request.getPriority());
        incident.setAssignee(request.getAssignee());

        // Handle status change
        if (request.getStatus() != null && incident.getStatus() != request.getStatus()) {
            incident.setStatus(request.getStatus());
            log.debug("Status changed from {} to {}",
                     incident.getStatus(), request.getStatus());
        }

        return incidentRepository.save(incident);
    }

    @Override
    @Transactional
    public Incident updateStatus(Long id, Status status) {
        log.info("Updating status of incident {} to {}", id, status);

        Incident incident = getIncidentById(id);
        incident.setStatus(status);

        // Note: resolvedAt will be set by @PreUpdate hook if status is RESOLVED
        return incidentRepository.save(incident);
    }

    @Override
    @Transactional
    public void deleteIncident(Long id) {
        log.info("Deleting incident with id: {}", id);

        Incident incident = getIncidentById(id);
        incidentRepository.delete(incident);
    }

    @Override
    public List<Incident> getIncidentsByStatus(Status status) {
        log.debug("Fetching incidents with status: {}", status);
        return incidentRepository.findByStatus(status);
    }

    @Override
    public List<Incident> getIncidentsByPriority(Priority priority) {
        log.debug("Fetching incidents with priority: {}", priority);
        return incidentRepository.findByPriority(priority);
    }

    @Override
    public List<Incident> getIncidentsByAssignee(String assignee) {
        log.debug("Fetching incidents for assignee: {}", assignee);
        return incidentRepository.findByAssignee(assignee);
    }

    @Override
    public List<Incident> getOverdueIncidents(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        log.debug("Fetching incidents older than {} days", daysOld);
        return incidentRepository.findOverdueIncidents(cutoff);
    }
}
