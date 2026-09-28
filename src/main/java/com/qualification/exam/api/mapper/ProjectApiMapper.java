package com.qualification.exam.api.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.qualification.exam.api.response.ProjectResponse;
import com.qualification.exam.api.response.TaskResponse;
import com.qualification.exam.entity.ProjectPlan;
import com.qualification.exam.entity.ProjectTask;

@Component
public class ProjectApiMapper {

    private final TaskApiMapper taskApiMapper;

    public ProjectApiMapper(
            TaskApiMapper taskApiMapper
    ) {
        this.taskApiMapper = taskApiMapper;
    }

    public ProjectResponse toResponse(
            ProjectPlan project,
            List<ProjectTask> tasks
    ) {
        List<TaskResponse> taskResponses =
                tasks.stream()
                        .map(taskApiMapper::toResponse)
                        .toList();

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getStartDate(),
                project.getCalculatedEndDate(),
                project.getVersion(),
                taskResponses
        );
    }

    public ProjectResponse toSummary(
            ProjectPlan project
    ) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getStartDate(),
                project.getCalculatedEndDate(),
                project.getVersion(),
                List.of()
        );
    }
}