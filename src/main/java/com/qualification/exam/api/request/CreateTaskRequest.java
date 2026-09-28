package com.qualification.exam.api.request;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(

        @NotBlank(message = "Task key is required")
        @Size(max = 50)
        String taskKey,

        @NotBlank(message = "Task name is required")
        @Size(max = 200)
        String name,

        @Min(value = 1)
        @Max(value = 3650)
        int durationDays,

        List<Long> dependencyIds

) {
    public CreateTaskRequest {
        dependencyIds = dependencyIds == null
                ? List.of()
                : List.copyOf(dependencyIds);
    }
}