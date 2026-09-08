package com.qualification.exam.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qualification.exam.dto.ProjectTaskForm;
import com.qualification.exam.entity.ProjectPlan;
import com.qualification.exam.entity.ProjectTask;
import com.qualification.exam.exception.InvalidProjectPlanException;
import com.qualification.exam.repository.ProjectPlanRepository;
import com.qualification.exam.repository.ProjectTaskRepository;

@Service
public class ProjectTaskService {

    private final ProjectPlanRepository projectPlanRepository;
    private final ProjectTaskRepository projectTaskRepository;

    public ProjectTaskService(
            ProjectPlanRepository projectPlanRepository,
            ProjectTaskRepository projectTaskRepository
    ) {
        this.projectPlanRepository = projectPlanRepository;
        this.projectTaskRepository = projectTaskRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectTask> findByProjectId(Long projectId) {
        return projectTaskRepository
                .findByProjectPlanIdOrderByIdAsc(projectId);
    }

    @Transactional
    public ProjectTask create(
            Long projectId,
            ProjectTaskForm form
    ) {
        ProjectPlan project = projectPlanRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new InvalidProjectPlanException(
                                "Project not found: " + projectId
                        )
                );

        String normalizedTaskKey = form.getTaskKey()
                .trim()
                .toUpperCase();

        boolean taskKeyExists = projectTaskRepository
                .existsByProjectPlanIdAndTaskKey(
                        projectId,
                        normalizedTaskKey
                );

        if (taskKeyExists) {
            throw new InvalidProjectPlanException(
                    "Task key already exists in this project: "
                            + normalizedTaskKey
            );
        }

        ProjectTask task = new ProjectTask(
                normalizedTaskKey,
                form.getName().trim(),
                form.getDurationDays()
        );

        task.assignToProject(project);

        Set<Long> dependencyIds = new LinkedHashSet<>();

        if (form.getDependencyIds() != null) {
            dependencyIds.addAll(
                    form.getDependencyIds()
            );
        }

        for (Long dependencyId : dependencyIds) {
            ProjectTask dependency = projectTaskRepository
                    .findById(dependencyId)
                    .orElseThrow(() ->
                            new InvalidProjectPlanException(
                                    "Dependency task not found: "
                                            + dependencyId
                            )
                    );

            Long dependencyProjectId = dependency
                    .getProjectPlan()
                    .getId();

            if (!projectId.equals(dependencyProjectId)) {
                throw new InvalidProjectPlanException(
                        "A task cannot depend on a task "
                                + "from another project"
                );
            }

            task.addDependency(dependency);
        }

        /*
         * Adding a task makes any previously calculated
         * project schedule outdated.
         */
        project.setCalculatedEndDate(null);

        return projectTaskRepository.save(task);
    }
    
    @Transactional
    public String delete(
            Long projectId,
            Long taskId
    ) {
        ProjectTask taskToDelete = projectTaskRepository
                .findById(taskId)
                .orElseThrow(() ->
                        new InvalidProjectPlanException(
                                "Task not found: " + taskId
                        )
                );

        Long taskProjectId = taskToDelete
                .getProjectPlan()
                .getId();

        String deletedTaskKey = taskToDelete.getTaskKey();

        if (!projectId.equals(taskProjectId)) {
            throw new InvalidProjectPlanException(
                    "Task does not belong to project: "
                            + projectId
            );
        }

        List<ProjectTask> projectTasks =
                projectTaskRepository
                        .findByProjectPlanIdOrderByIdAsc(
                                projectId
                        );

        for (ProjectTask projectTask : projectTasks) {
            if (!projectTask.getId().equals(taskId)) {
                projectTask.removeDependency(taskToDelete);
            }
        }

        taskToDelete.clearDependencies();

        projectTaskRepository.saveAll(projectTasks);
        projectTaskRepository.flush();

        projectTaskRepository.delete(taskToDelete);
        projectTaskRepository.flush();

        return deletedTaskKey;
    }
    @Transactional(readOnly = true)
    public boolean hasTasks(Long projectId) {
        return projectTaskRepository
                .existsByProjectPlanId(projectId);
    }
    
    @Transactional
    public void clearProjectEndDate(Long projectId) {
        ProjectPlan project = projectPlanRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new InvalidProjectPlanException(
                                "Project not found: "
                                        + projectId
                        )
                );

        project.setCalculatedEndDate(null);

        projectPlanRepository.save(project);
    }
}