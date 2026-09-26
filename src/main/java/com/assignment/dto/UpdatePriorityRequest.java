package com.assignment.dto;

import com.assignment.enums.Priority;

import jakarta.validation.constraints.NotNull;

public class UpdatePriorityRequest {

    @NotNull(message = "Priority is required")
    private Priority priority;

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}