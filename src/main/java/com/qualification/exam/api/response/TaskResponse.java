package com.qualification.exam.api.response;

import java.time.LocalDate;
import java.util.List;

public record TaskResponse(
        Long id,
        String taskKey,
        String name,
        int durationDays,
        List<Long> dependencyIds,
        LocalDate calculatedStartDate,
        LocalDate calculatedEndDate,
        Long version
) {
}