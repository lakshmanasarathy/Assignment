package com.assignment.dto;

import java.util.Locale.Category;

import com.assignment.enums.Priority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateTicketRequest {

    @NotBlank
    private String subject;

    @NotBlank
    private String description;

    @NotNull
    private Category category;

    @NotNull
    private Priority priority;

    // getters/setters
}