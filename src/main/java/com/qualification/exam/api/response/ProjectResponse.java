package com.qualification.exam.api.response;

import java.time.LocalDate;
import java.util.List;

public record ProjectResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate calculatedEndDate,
        Long version,
        List<TaskResponse> tasks
) {
}