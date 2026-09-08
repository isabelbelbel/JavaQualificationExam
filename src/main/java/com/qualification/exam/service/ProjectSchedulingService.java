package com.qualification.exam.service;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qualification.exam.entity.ProjectPlan;
import com.qualification.exam.entity.ProjectTask;
import com.qualification.exam.exception.CircularDependencyException;
import com.qualification.exam.exception.InvalidProjectPlanException;
import com.qualification.exam.repository.ProjectPlanRepository;
import com.qualification.exam.repository.ProjectTaskRepository;

@Service
public class ProjectSchedulingService {

    private final ProjectPlanRepository projectPlanRepository;
    private final ProjectTaskRepository projectTaskRepository;

    public ProjectSchedulingService(
            ProjectPlanRepository projectPlanRepository,
            ProjectTaskRepository projectTaskRepository
    ) {
        this.projectPlanRepository = projectPlanRepository;
        this.projectTaskRepository = projectTaskRepository;
    }

    @Transactional
    public void calculateSchedule(Long projectId) {
        ProjectPlan project = projectPlanRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new InvalidProjectPlanException(
                                "Project not found: " + projectId
                        )
                );

        List<ProjectTask> tasks = projectTaskRepository
                .findByProjectPlanIdOrderByIdAsc(projectId);

        if (tasks.isEmpty()) {
            throw new InvalidProjectPlanException(
                    "Cannot calculate a schedule "
                            + "for a project with no tasks"
            );
        }

        for (ProjectTask task : tasks) {
            task.clearSchedule();
        }

        Map<Long, ProjectTask> tasksById =
                createTaskIndex(tasks);

        validateDependencies(
                projectId,
                tasks,
                tasksById
        );

        Map<Long, Integer> inDegree =
                createInDegreeMap(tasks);

        Map<Long, Set<ProjectTask>> dependents =
                createDependentsMap(tasks);

        Queue<ProjectTask> readyTasks =
                createReadyTaskQueue(tasks, inDegree);

        Map<Long, ProjectTask> scheduledTasks =
                new LinkedHashMap<>();

        while (!readyTasks.isEmpty()) {
            ProjectTask currentTask =
                    readyTasks.poll();

            LocalDate startDate = calculateStartDate(
                    project.getStartDate(),
                    currentTask
            );

            LocalDate endDate = startDate.plusDays(
                    currentTask.getDurationDays() - 1L
            );

            currentTask.updateSchedule(
                    startDate,
                    endDate
            );

            scheduledTasks.put(
                    currentTask.getId(),
                    currentTask
            );

            List<ProjectTask> newlyReadyTasks =
                    new ArrayList<>();

            for (ProjectTask dependent :
                    dependents.getOrDefault(
                            currentTask.getId(),
                            Set.of()
                    )) {

                int remainingDependencies =
                        inDegree.compute(
                                dependent.getId(),
                                (taskId, currentValue) ->
                                        currentValue - 1
                        );

                if (remainingDependencies == 0) {
                    newlyReadyTasks.add(dependent);
                }
            }

            newlyReadyTasks.sort(
                    Comparator.comparing(
                            ProjectTask::getTaskKey
                    )
            );

            readyTasks.addAll(newlyReadyTasks);
        }

        detectCircularDependencies(
                tasks,
                scheduledTasks
        );

        LocalDate projectEndDate = tasks.stream()
                .map(ProjectTask::getCalculatedEndDate)
                .max(Comparator.naturalOrder())
                .orElse(project.getStartDate());

        project.setCalculatedEndDate(
                projectEndDate
        );

        projectTaskRepository.saveAll(tasks);
        projectPlanRepository.save(project);
    }

    private Map<Long, ProjectTask> createTaskIndex(
            List<ProjectTask> tasks
    ) {
        Map<Long, ProjectTask> tasksById =
                new LinkedHashMap<>();

        for (ProjectTask task : tasks) {
            tasksById.put(task.getId(), task);
        }

        return tasksById;
    }

    private void validateDependencies(
            Long projectId,
            List<ProjectTask> tasks,
            Map<Long, ProjectTask> tasksById
    ) {
        for (ProjectTask task : tasks) {
            if (task.getDurationDays() < 1) {
                throw new InvalidProjectPlanException(
                        "Task " + task.getTaskKey()
                                + " must have a duration "
                                + "of at least one day"
                );
            }

            for (ProjectTask dependency :
                    task.getDependencies()) {

                if (dependency.getId()
                        .equals(task.getId())) {

                    throw new InvalidProjectPlanException(
                            "Task " + task.getTaskKey()
                                    + " cannot depend on itself"
                    );
                }

                if (!tasksById.containsKey(
                        dependency.getId()
                )) {
                    throw new InvalidProjectPlanException(
                            "Task " + task.getTaskKey()
                                    + " has a missing or invalid "
                                    + "dependency"
                    );
                }

                Long dependencyProjectId =
                        dependency.getProjectPlan()
                                .getId();

                if (!projectId.equals(
                        dependencyProjectId
                )) {
                    throw new InvalidProjectPlanException(
                            "Task " + task.getTaskKey()
                                    + " depends on a task "
                                    + "from another project"
                    );
                }
            }
        }
    }

    private Map<Long, Integer> createInDegreeMap(
            List<ProjectTask> tasks
    ) {
        Map<Long, Integer> inDegree =
                new HashMap<>();

        for (ProjectTask task : tasks) {
            inDegree.put(
                    task.getId(),
                    task.getDependencies().size()
            );
        }

        return inDegree;
    }

    private Map<Long, Set<ProjectTask>>
    createDependentsMap(List<ProjectTask> tasks) {

        Map<Long, Set<ProjectTask>> dependents =
                new HashMap<>();

        for (ProjectTask task : tasks) {
            dependents.put(
                    task.getId(),
                    new LinkedHashSet<>()
            );
        }

        for (ProjectTask task : tasks) {
            for (ProjectTask dependency :
                    task.getDependencies()) {

                dependents
                        .get(dependency.getId())
                        .add(task);
            }
        }

        return dependents;
    }

    private Queue<ProjectTask> createReadyTaskQueue(
            List<ProjectTask> tasks,
            Map<Long, Integer> inDegree
    ) {
        Queue<ProjectTask> readyTasks =
                new ArrayDeque<>();

        tasks.stream()
                .filter(task ->
                        inDegree.get(task.getId()) == 0
                )
                .sorted(
                        Comparator.comparing(
                                ProjectTask::getTaskKey
                        )
                )
                .forEach(readyTasks::offer);

        return readyTasks;
    }

    private LocalDate calculateStartDate(
            LocalDate projectStartDate,
            ProjectTask task
    ) {
        return task.getDependencies()
                .stream()
                .map(ProjectTask::getCalculatedEndDate)
                .filter(date -> date != null)
                .max(Comparator.naturalOrder())
                .map(date -> date.plusDays(1))
                .orElse(projectStartDate);
    }

    private void detectCircularDependencies(
            List<ProjectTask> tasks,
            Map<Long, ProjectTask> scheduledTasks
    ) {
        if (scheduledTasks.size() == tasks.size()) {
            return;
        }

        Set<String> affectedTaskKeys =
                new LinkedHashSet<>();

        for (ProjectTask task : tasks) {
            if (!scheduledTasks.containsKey(
                    task.getId()
            )) {
                affectedTaskKeys.add(
                        task.getTaskKey()
                );
            }
        }

        throw new CircularDependencyException(
                affectedTaskKeys
        );
    }
}