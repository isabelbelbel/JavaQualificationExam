package com.qualification.exam.api.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(

        @NotBlank(message = "Project name is required")
        @Size(
                max = 150,
                message = "Project name cannot exceed 150 characters"
        )
        String name,

        @NotNull(message = "Project start date is required")
        LocalDate startDate

) {
}