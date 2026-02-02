package com.example.incidenttracker.repository;

import com.example.incidenttracker.model.Incident;
import com.example.incidenttracker.model.Priority;
import com.example.incidenttracker.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    /**
     * Find all incidents by status.
     */
    List<Incident> findByStatus(Status status);

    /**
     * Find all incidents by priority.
     */
    List<Incident> findByPriority(Priority priority);

    /**
     * Find all incidents assigned to a specific person.
     */
    List<Incident> findByAssignee(String assignee);

    /**
     * Find incidents by both status and priority.
     */
    List<Incident> findByStatusAndPriority(Status status, Priority priority);

    /**
     * Find incidents that are open or in progress and older than a specified date.
     * Useful for identifying overdue incidents.
     *
     * @param cutoffDate the cutoff date
     * @return list of overdue incidents
     */
    @Query("SELECT i FROM Incident i WHERE i.status IN ('OPEN', 'IN_PROGRESS') " +
           "AND i.createdAt < :cutoffDate")
    List<Incident> findOverdueIncidents(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count incidents by status.
     */
    long countByStatus(Status status);

    /**
     * Count incidents by priority.
     */
    long countByPriority(Priority priority);
}
