package com.assignment.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateResolutionRequest {

    @NotBlank(message = "Resolution is required")
    private String resolution;

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}