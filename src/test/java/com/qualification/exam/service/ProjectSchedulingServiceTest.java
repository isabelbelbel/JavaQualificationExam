package com.qualification.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.qualification.exam.entity.ProjectPlan;
import com.qualification.exam.entity.ProjectTask;
import com.qualification.exam.exception.CircularDependencyException;
import com.qualification.exam.exception.InvalidProjectPlanException;
import com.qualification.exam.repository.ProjectPlanRepository;
import com.qualification.exam.repository.ProjectTaskRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectSchedulingServiceTest {

	@Autowired
	private ProjectSchedulingService projectSchedulingService;

	@Autowired
	private ProjectPlanRepository projectPlanRepository;

	@Autowired
	private ProjectTaskRepository projectTaskRepository;

	private ProjectPlan project;

	@BeforeEach
	void setUp() {
		projectTaskRepository.deleteAll();
		projectPlanRepository.deleteAll();

		project = new ProjectPlan("Scheduling Test", LocalDate.of(2026, 9, 10));

		project = projectPlanRepository.saveAndFlush(project);
	}

	@Test
	void shouldCalculateOneDayTask() {
		ProjectTask task = createTask("A", "One Day Task", 1);

		projectSchedulingService.calculateSchedule(project.getId());

		ProjectTask calculatedTask = findTask(task.getId());

		assertEquals(LocalDate.of(2026, 9, 10), calculatedTask.getCalculatedStartDate());

		assertEquals(LocalDate.of(2026, 9, 10), calculatedTask.getCalculatedEndDate());
	}

	@Test
	void shouldScheduleIndependentTasksInParallel() {
		ProjectTask taskA = createTask("A", "Gather Requirements", 3);

		ProjectTask taskB = createTask("B", "Prepare Environment", 5);

		projectSchedulingService.calculateSchedule(project.getId());

		ProjectTask calculatedTaskA = findTask(taskA.getId());
		ProjectTask calculatedTaskB = findTask(taskB.getId());

		assertEquals(LocalDate.of(2026, 9, 10), calculatedTaskA.getCalculatedStartDate());

		assertEquals(LocalDate.of(2026, 9, 12), calculatedTaskA.getCalculatedEndDate());

		assertEquals(LocalDate.of(2026, 9, 10), calculatedTaskB.getCalculatedStartDate());

		assertEquals(LocalDate.of(2026, 9, 14), calculatedTaskB.getCalculatedEndDate());
	}

	@Test
	void shouldStartDependentTaskAfterPredecessorEnds() {
		ProjectTask taskA = createTask("A", "Gather Requirements", 3);

		ProjectTask taskB = createTask("B", "Design Solution", 2);

		taskB.addDependency(taskA);
		projectTaskRepository.saveAndFlush(taskB);

		projectSchedulingService.calculateSchedule(project.getId());

		ProjectTask calculatedTaskA = findTask(taskA.getId());
		ProjectTask calculatedTaskB = findTask(taskB.getId());

		assertEquals(LocalDate.of(2026, 9, 10), calculatedTaskA.getCalculatedStartDate());

		assertEquals(LocalDate.of(2026, 9, 12), calculatedTaskA.getCalculatedEndDate());

		assertEquals(LocalDate.of(2026, 9, 13), calculatedTaskB.getCalculatedStartDate());

		assertEquals(LocalDate.of(2026, 9, 14), calculatedTaskB.getCalculatedEndDate());
	}

	@Test
	void shouldStartAfterLatestDependencyEnds() {
		ProjectTask taskA = createTask("A", "Gather Requirements", 3);

		ProjectTask taskB = createTask("B", "Prepare Environment", 5);

		ProjectTask taskC = createTask("C", "Implement Solution", 2);

		taskC.addDependency(taskA);
		taskC.addDependency(taskB);

		projectTaskRepository.saveAndFlush(taskC);

		projectSchedulingService.calculateSchedule(project.getId());

		ProjectTask calculatedTaskC = findTask(taskC.getId());

		assertEquals(LocalDate.of(2026, 9, 15), calculatedTaskC.getCalculatedStartDate());

		assertEquals(LocalDate.of(2026, 9, 16), calculatedTaskC.getCalculatedEndDate());
	}

	@Test
	void shouldCalculateDependencyChain() {
		ProjectTask taskA = createTask("A", "Requirements", 3);

		ProjectTask taskB = createTask("B", "Design", 2);

		ProjectTask taskC = createTask("C", "Development", 4);

		taskB.addDependency(taskA);
		taskC.addDependency(taskB);

		projectTaskRepository.saveAllAndFlush(List.of(taskB, taskC));

		projectSchedulingService.calculateSchedule(project.getId());

		ProjectTask calculatedTaskA = findTask(taskA.getId());
		ProjectTask calculatedTaskB = findTask(taskB.getId());
		ProjectTask calculatedTaskC = findTask(taskC.getId());

		assertEquals(LocalDate.of(2026, 9, 10), calculatedTaskA.getCalculatedStartDate());

		assertEquals(LocalDate.of(2026, 9, 12), calculatedTaskA.getCalculatedEndDate());

		assertEquals(LocalDate.of(2026, 9, 13), calculatedTaskB.getCalculatedStartDate());

		assertEquals(LocalDate.of(2026, 9, 14), calculatedTaskB.getCalculatedEndDate());

		assertEquals(LocalDate.of(2026, 9, 15), calculatedTaskC.getCalculatedStartDate());

		assertEquals(LocalDate.of(2026, 9, 18), calculatedTaskC.getCalculatedEndDate());
	}

	@Test
	void shouldSetProjectEndDateToLatestTaskEndDate() {
		ProjectTask taskA = createTask("A", "Short Task", 2);

		ProjectTask taskB = createTask("B", "Long Task", 6);

		ProjectTask taskC = createTask("C", "Dependent Task", 3);

		taskC.addDependency(taskA);
		projectTaskRepository.saveAndFlush(taskC);

		projectSchedulingService.calculateSchedule(project.getId());

		ProjectPlan calculatedProject = projectPlanRepository.findById(project.getId()).orElseThrow();

		assertEquals(LocalDate.of(2026, 9, 15), calculatedProject.getCalculatedEndDate());

		assertEquals(LocalDate.of(2026, 9, 15), findTask(taskB.getId()).getCalculatedEndDate());
	}

	@Test
	void shouldRejectProjectWithNoTasks() {
		InvalidProjectPlanException exception = assertThrows(InvalidProjectPlanException.class,
				() -> projectSchedulingService.calculateSchedule(project.getId()));

		assertEquals("Cannot calculate a schedule for a project with no tasks", exception.getMessage());
	}

	@Test
	void shouldRejectTaskWithInvalidDuration() {
		ProjectTask task = createTask("A", "Invalid Task", 0);

		InvalidProjectPlanException exception = assertThrows(InvalidProjectPlanException.class,
				() -> projectSchedulingService.calculateSchedule(project.getId()));

		assertEquals("Task A must have a duration of at least one day", exception.getMessage());
	}

	@Test
	void shouldRejectSelfDependency() {
		ProjectTask task = createTask("A", "Self-Dependent Task", 2);

		task.addDependency(task);
		projectTaskRepository.saveAndFlush(task);

		InvalidProjectPlanException exception = assertThrows(InvalidProjectPlanException.class,
				() -> projectSchedulingService.calculateSchedule(project.getId()));

		assertEquals("Task A cannot depend on itself", exception.getMessage());
	}

	@Test
	void shouldDetectCircularDependencies() {
		ProjectTask taskA = createTask("A", "Task A", 2);

		ProjectTask taskB = createTask("B", "Task B", 2);

		ProjectTask taskC = createTask("C", "Task C", 2);

		taskA.addDependency(taskC);
		taskB.addDependency(taskA);
		taskC.addDependency(taskB);

		projectTaskRepository.saveAllAndFlush(List.of(taskA, taskB, taskC));

		CircularDependencyException exception = assertThrows(CircularDependencyException.class,
				() -> projectSchedulingService.calculateSchedule(project.getId()));

		assertEquals(3, exception.getAffectedTaskKeys().size());

		assertEquals(true, exception.getAffectedTaskKeys().contains("A"));

		assertEquals(true, exception.getAffectedTaskKeys().contains("B"));

		assertEquals(true, exception.getAffectedTaskKeys().contains("C"));
	}

	@Test
	void shouldRecalculatePreviouslyCalculatedDates() {
		ProjectTask taskA = createTask("A", "Task A", 2);

		ProjectTask taskB = createTask("B", "Task B", 3);

		projectSchedulingService.calculateSchedule(project.getId());

		assertEquals(LocalDate.of(2026, 9, 10), findTask(taskB.getId()).getCalculatedStartDate());

		taskB.addDependency(taskA);
		projectTaskRepository.saveAndFlush(taskB);

		projectSchedulingService.calculateSchedule(project.getId());

		ProjectTask recalculatedTaskB = findTask(taskB.getId());

		assertEquals(LocalDate.of(2026, 9, 12), recalculatedTaskB.getCalculatedStartDate());

		assertEquals(LocalDate.of(2026, 9, 14), recalculatedTaskB.getCalculatedEndDate());
	}

	private ProjectTask createTask(String taskKey, String taskName, int durationDays) {
		ProjectTask task = new ProjectTask(taskKey, taskName, durationDays);

		task.assignToProject(project);

		return projectTaskRepository.saveAndFlush(task);
	}

	private ProjectTask findTask(Long taskId) {
		return projectTaskRepository.findById(taskId).orElseThrow();
	}
}