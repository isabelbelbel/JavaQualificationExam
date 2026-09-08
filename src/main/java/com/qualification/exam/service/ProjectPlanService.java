package com.qualification.exam.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qualification.exam.dto.ProjectPlanForm;
import com.qualification.exam.entity.ProjectPlan;
import com.qualification.exam.entity.ProjectTask;
import com.qualification.exam.exception.InvalidProjectPlanException;
import com.qualification.exam.repository.ProjectPlanRepository;
import com.qualification.exam.repository.ProjectTaskRepository;

@Service
public class ProjectPlanService {

    private final ProjectPlanRepository projectPlanRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final AuditLogService auditLogService;
    
    public ProjectPlanService(
            ProjectPlanRepository projectPlanRepository,
            ProjectTaskRepository projectTaskRepository,
            AuditLogService auditLogService
    ) {
        this.projectPlanRepository = projectPlanRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.auditLogService = auditLogService;
    }
    
    @Transactional(readOnly = true)
    public List<ProjectPlan> findAll() {
        return projectPlanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProjectPlan findById(Long id) {
        return projectPlanRepository.findWithTasksById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + id
                ));
    }
    
    @Transactional
    public ProjectPlan create(ProjectPlanForm form) {
        ProjectPlan projectPlan = new ProjectPlan(
                form.getName().trim(),
                form.getStartDate()
        );

        ProjectPlan savedProject =
                projectPlanRepository.save(projectPlan);

        auditLogService.record(
                "CREATE_PROJECT",
                savedProject.getId(),
                null,
                "Created project " + savedProject.getName()
        );

        return savedProject;
    }
    
    @Transactional
    public void delete(Long projectId) {
        ProjectPlan project = projectPlanRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new InvalidProjectPlanException(
                                "Project not found: " + projectId
                        )
                );

        String deletedProjectName = project.getName();

        List<ProjectTask> tasks =
                projectTaskRepository
                        .findByProjectPlanIdOrderByIdAsc(
                                projectId
                        );

        for (ProjectTask task : tasks) {
            task.clearDependencies();
        }

        projectTaskRepository.saveAll(tasks);
        projectTaskRepository.flush();

        projectPlanRepository.delete(project);
        projectPlanRepository.flush();

        auditLogService.record(
                "DELETE_PROJECT",
                projectId,
                null,
                "Deleted project "
                        + deletedProjectName
                        + " and all associated tasks"
        );
    }
}