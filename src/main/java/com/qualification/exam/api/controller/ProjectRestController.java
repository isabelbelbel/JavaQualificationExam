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
import com.qualification.exam.api.response.ProjectResponse;
import com.qualification.exam.dto.ProjectPlanForm;
import com.qualification.exam.entity.ProjectPlan;
import com.qualification.exam.entity.ProjectTask;
import com.qualification.exam.service.ProjectPlanService;
import com.qualification.exam.service.ProjectTaskService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(
        name = "Projects",
        description = "Project plan management operations"
)
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/projects")
public class ProjectRestController {

    private final ProjectPlanService projectPlanService;
    private final ProjectTaskService projectTaskService;
    private final ProjectApiMapper projectApiMapper;

    public ProjectRestController(
            ProjectPlanService projectPlanService,
            ProjectTaskService projectTaskService,
            ProjectApiMapper projectApiMapper
    ) {
        this.projectPlanService = projectPlanService;
        this.projectTaskService = projectTaskService;
        this.projectApiMapper = projectApiMapper;
    }

    
    @Operation(summary = "List all project plans")
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects() {
        List<ProjectResponse> response =
                projectPlanService.findAll()
                        .stream()
                        .map(projectApiMapper::toSummary)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve one project plan")
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable("projectId") Long projectId
    ) {
        ProjectPlan project =
                projectPlanService.findById(projectId);

        List<ProjectTask> tasks =
                projectTaskService.findByProjectId(projectId);

        ProjectResponse response =
                projectApiMapper.toResponse(
                        project,
                        tasks
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a project plan")
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectPlanForm form
    ) {
        ProjectPlan createdProject =
                projectPlanService.create(form);

        ProjectResponse response =
                projectApiMapper.toSummary(createdProject);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    @Operation(summary = "Delete a project plan")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable("projectId") Long projectId
    ) {
        projectPlanService.delete(projectId);

        return ResponseEntity
                .noContent()
                .build();
    }
}