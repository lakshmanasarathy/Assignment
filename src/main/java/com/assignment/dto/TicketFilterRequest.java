package com.assignment.dto;

import com.assignment.enums.Category;
import com.assignment.enums.Priority;
import com.assignment.enums.TicketStatus;

public class TicketFilterRequest {

    private TicketStatus status;
    private Priority priority;
    private Category category;
    private String assignment;

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getAssignment() {
        return assignment;
    }

    public void setAssignment(String assignment) {
        this.assignment = assignment;
    }
}