package com.example.incidenttracker.model;

public enum Status {
    OPEN("New incident, not yet assigned"),
    IN_PROGRESS("Actively being worked on"),
    RESOLVED("Issue resolved, pending verification"),
    CLOSED("Verified and closed");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
