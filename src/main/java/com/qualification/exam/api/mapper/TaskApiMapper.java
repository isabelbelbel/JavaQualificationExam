package com.qualification.exam.api.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.qualification.exam.api.response.TaskResponse;
import com.qualification.exam.entity.ProjectTask;

@Component
public class TaskApiMapper {

    public TaskResponse toResponse(
            ProjectTask task
    ) {
        List<Long> dependencyIds =
                task.getDependencies()
                        .stream()
                        .map(ProjectTask::getId)
                        .sorted()
                        .toList();

        return new TaskResponse(
                task.getId(),
                task.getTaskKey(),
                task.getName(),
                task.getDurationDays(),
                dependencyIds,
                task.getCalculatedStartDate(),
                task.getCalculatedEndDate(),
                task.getVersion()
        );
    }
}