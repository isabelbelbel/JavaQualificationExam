package com.qualification.exam.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.qualification.exam.api.response.ProjectResponse;
import com.qualification.exam.entity.ProjectPlan;

class ProjectApiMapperTest {

	private final TaskApiMapper taskApiMapper = new TaskApiMapper();

	private final ProjectApiMapper projectApiMapper = new ProjectApiMapper(taskApiMapper);

	@Test
	void shouldMapProjectToSummary() {
		// Arrange
		ProjectPlan project = new ProjectPlan("Unit Test Project", LocalDate.of(2026, 9, 20));

		// Act
		ProjectResponse response = projectApiMapper.toSummary(project);

		// Assert
		assertNull(response.id());

		assertEquals("Unit Test Project", response.name());

		assertEquals(LocalDate.of(2026, 9, 20), response.startDate());

		assertNull(response.calculatedEndDate());

		assertNull(response.version());

		assertTrue(response.tasks().isEmpty());
	}

	@Test
	void shouldMapDetailedProjectWithoutTasks() {
		// Arrange
		ProjectPlan project = new ProjectPlan("Detailed Unit Test Project", LocalDate.of(2026, 10, 1));

		// Act
		ProjectResponse response = projectApiMapper.toResponse(project, List.of());

		// Assert
		assertEquals("Detailed Unit Test Project", response.name());

		assertEquals(LocalDate.of(2026, 10, 1), response.startDate());

		assertTrue(response.tasks().isEmpty());
	}
}