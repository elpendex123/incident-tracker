package com.example.incidenttracker.service;

import com.example.incidenttracker.dto.IncidentRequest;
import com.example.incidenttracker.model.Incident;
import com.example.incidenttracker.model.Priority;
import com.example.incidenttracker.model.Status;

import java.util.List;

/**
 * Service interface for incident management.
 * Defines business logic operations for incidents.
 */
public interface IncidentService {

    /**
     * Retrieve all incidents.
     *
     * @return list of all incidents
     */
    List<Incident> getAllIncidents();

    /**
     * Retrieve an incident by ID.
     *
     * @param id the incident ID
     * @return the incident
     * @throws com.example.incidenttracker.exception.ResourceNotFoundException if not found
     */
    Incident getIncidentById(Long id);

    /**
     * Create a new incident.
     *
     * @param request the incident request DTO
     * @return the created incident
     */
    Incident createIncident(IncidentRequest request);

    /**
     * Update an existing incident.
     *
     * @param id the incident ID
     * @param request the update request DTO
     * @return the updated incident
     * @throws com.example.incidenttracker.exception.ResourceNotFoundException if not found
     */
    Incident updateIncident(Long id, IncidentRequest request);

    /**
     * Update only the status of an incident.
     *
     * @param id the incident ID
     * @param status the new status
     * @return the updated incident
     * @throws com.example.incidenttracker.exception.ResourceNotFoundException if not found
     */
    Incident updateStatus(Long id, Status status);

    /**
     * Delete an incident.
     *
     * @param id the incident ID
     * @throws com.example.incidenttracker.exception.ResourceNotFoundException if not found
     */
    void deleteIncident(Long id);

    /**
     * Retrieve incidents filtered by status.
     *
     * @param status the status filter
     * @return list of incidents with the specified status
     */
    List<Incident> getIncidentsByStatus(Status status);

    /**
     * Retrieve incidents filtered by priority.
     *
     * @param priority the priority filter
     * @return list of incidents with the specified priority
     */
    List<Incident> getIncidentsByPriority(Priority priority);

    /**
     * Retrieve incidents assigned to a specific person.
     *
     * @param assignee the assignee name
     * @return list of incidents assigned to the person
     */
    List<Incident> getIncidentsByAssignee(String assignee);

    /**
     * Retrieve incidents that have been open/in-progress for more than N days.
     *
     * @param daysOld the number of days
     * @return list of overdue incidents
     */
    List<Incident> getOverdueIncidents(int daysOld);
}
