package com.qualification.exam.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qualification.exam.dto.ProjectPlanForm;
import com.qualification.exam.dto.ProjectTaskForm;
import com.qualification.exam.entity.ProjectPlan;
import com.qualification.exam.entity.ProjectTask;
import com.qualification.exam.service.ProjectPlanService;
import com.qualification.exam.service.ProjectTaskApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class JsonApiController {

	private final ProjectPlanService projectPlanService;

	private final ProjectTaskApplicationService projectTaskApplicationService;

	public JsonApiController(ProjectPlanService projectPlanService,
			ProjectTaskApplicationService projectTaskApplicationService) {
		this.projectPlanService = projectPlanService;
		this.projectTaskApplicationService = projectTaskApplicationService;
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> createProject(@Valid @RequestBody ProjectPlanForm form) {
		ProjectPlan createdProject = projectPlanService.create(form);

		Map<String, Object> response = new LinkedHashMap<>();

		response.put("id", createdProject.getId());
		response.put("name", createdProject.getName());
		response.put("startDate", createdProject.getStartDate());
		response.put("calculatedEndDate", createdProject.getCalculatedEndDate());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/{projectId}/tasks")
    public ResponseEntity<Map<String, Object>> createTask(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ProjectTaskForm form
    ) {
        ProjectTask createdTask =
                projectTaskApplicationService
                        .createAndRecalculate(
                                projectId,
                                form
                        );

        ProjectPlan updatedProject =
                projectPlanService.findById(projectId);

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("projectId", projectId);
        response.put(
                "projectEndDate",
                updatedProject.getCalculatedEndDate()
        );

        response.put("taskId", createdTask.getId());
        response.put(
                "taskKey",
                createdTask.getTaskKey()
        );
        response.put("name", createdTask.getName());

		response.put(
		        "durationDays",
		        createdTask.getDurationDays()
		);
		
		response.put(
		        "calculatedStartDate",
		        createdTask.getCalculatedStartDate()
		);
		
		response.put(
		        "calculatedEndDate",
		        createdTask.getCalculatedEndDate()
		);
		
		response.put(
		        "dependencyIds",
		        createdTask.getDependencies()
		                .stream()
		                .map(ProjectTask::getId)
		                .sorted()
		                .toList()
		);
		
		return ResponseEntity.ok(response);
    }
}