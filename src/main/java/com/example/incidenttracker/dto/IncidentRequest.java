package com.example.incidenttracker.dto;

import com.example.incidenttracker.model.Priority;
import com.example.incidenttracker.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Builder.Default
    private Priority priority = Priority.LOW;

    @Builder.Default
    private Status status = Status.OPEN;

    @Size(max = 100, message = "Assignee name must not exceed 100 characters")
    private String assignee;
}
