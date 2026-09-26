package com.assignment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AssignTicketRequest {

    @NotBlank(message = "Staff email is required")
    @Email(message = "Invalid staff email")
    private String staffEmail;

    public String getStaffEmail() {
        return staffEmail;
    }

    public void setStaffEmail(String staffEmail) {
        this.staffEmail = staffEmail;
    }
}