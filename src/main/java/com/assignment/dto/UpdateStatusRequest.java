package com.assignment.dto;

import com.assignment.enums.TicketStatus;

import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private TicketStatus status;

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}