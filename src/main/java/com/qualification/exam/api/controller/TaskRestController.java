package com.qualification.exam.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qualification.exam.api.mapper.ProjectApiMapper;
import com.qualification.exam.api.mapper.TaskApiMapper;
import com.qualification.exam.api.response.ProjectResponse;
import com.qualification.exam.api.response.TaskResponse;
import com.qualification.exam.dto.ProjectTaskForm;
import com.qualification.exam.entity.ProjectPlan;
import com.qualification.exam.entity.ProjectTask;
import com.qualification.exam.service.ProjectPlanService;
import com.qualification.exam.service.ProjectTaskApplicationService;
import com.qualification.exam.service.ProjectTaskService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Tasks",
        description = "Project-task management operations"
)
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskRestController {

    private final ProjectPlanService projectPlanService;
    private final ProjectTaskService projectTaskService;
    private final ProjectTaskApplicationService
            projectTaskApplicationService;
    private final ProjectApiMapper projectApiMapper;
    private final TaskApiMapper taskApiMapper;

    public TaskRestController(
            ProjectPlanService projectPlanService,
            ProjectTaskService projectTaskService,
            ProjectTaskApplicationService projectTaskApplicationService,
            ProjectApiMapper projectApiMapper,
            TaskApiMapper taskApiMapper
    ) {
        this.projectPlanService = projectPlanService;
        this.projectTaskService = projectTaskService;
        this.projectTaskApplicationService =
                projectTaskApplicationService;
        this.projectApiMapper = projectApiMapper;
        this.taskApiMapper = taskApiMapper;
    }
    
    @Operation(summary = "List all tasks in a project")
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @PathVariable("projectId") Long projectId
    ) {
        projectPlanService.findById(projectId);

        List<TaskResponse> response =
                projectTaskService.findByProjectId(projectId)
                        .stream()
                        .map(taskApiMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List all tasks in a project")
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable("projectId") Long projectId,
            @PathVariable("taskId") Long taskId
    ) {
        ProjectTask task =
                projectTaskService.findByProjectIdAndTaskId(
                        projectId,
                        taskId
                );

        return ResponseEntity.ok(
                taskApiMapper.toResponse(task)
        );
    }

	@Operation(summary = "Create a task and recalculate the schedule")
    @PostMapping
    public ResponseEntity<ProjectResponse> createTask(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ProjectTaskForm form
    ) {
        projectTaskApplicationService.createAndRecalculate(
                projectId,
                form
        );

        ProjectResponse response =
                loadProjectResponse(projectId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

	@Operation(summary = "Delete a task and recalculate the schedule")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable("projectId") Long projectId,
            @PathVariable("taskId") Long taskId
    ) {
        projectTaskApplicationService.deleteAndRecalculate(
                projectId,
                taskId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    private ProjectResponse loadProjectResponse(
            Long projectId
    ) {
        ProjectPlan project =
                projectPlanService.findById(projectId);

        List<ProjectTask> tasks =
                projectTaskService.findByProjectId(projectId);

        return projectApiMapper.toResponse(
                project,
                tasks
        );
    }
}