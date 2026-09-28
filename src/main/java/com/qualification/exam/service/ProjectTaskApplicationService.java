package com.qualification.exam.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qualification.exam.dto.ProjectTaskForm;
import com.qualification.exam.entity.ProjectTask;

@Service
public class ProjectTaskApplicationService {

	private final ProjectTaskService projectTaskService;

	private final ProjectSchedulingService projectSchedulingService;

	private final AuditLogService auditLogService;

	public ProjectTaskApplicationService(ProjectTaskService projectTaskService,
			ProjectSchedulingService projectSchedulingService, AuditLogService auditLogService) {
		this.projectTaskService = projectTaskService;
		this.projectSchedulingService = projectSchedulingService;
		this.auditLogService = auditLogService;
	}

	@Transactional
	public ProjectTask createAndRecalculate(Long projectId, ProjectTaskForm form) {
		ProjectTask createdTask = projectTaskService.create(projectId, form);

		projectSchedulingService.calculateSchedule(projectId);

		auditLogService.record("CREATE_TASK", projectId, createdTask.getId(),
				"Created task " + createdTask.getTaskKey() + " and recalculated the schedule");

		return createdTask;
	}

	@Transactional
	public void deleteAndRecalculate(Long projectId, Long taskId) {
		String deletedTaskKey = projectTaskService.delete(projectId, taskId);

		if (projectTaskService.hasTasks(projectId)) {
			projectSchedulingService.calculateSchedule(projectId);
		} else {
			projectTaskService.clearProjectEndDate(projectId);
		}

		auditLogService.record("DELETE_TASK", projectId, taskId,
				"Deleted task " + deletedTaskKey + " and recalculated the schedule");
	}
}