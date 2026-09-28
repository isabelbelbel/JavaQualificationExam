package com.qualification.exam.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.qualification.exam.api.response.TaskResponse;
import com.qualification.exam.entity.ProjectTask;

class TaskApiMapperTest {

	private final TaskApiMapper taskApiMapper = new TaskApiMapper();

	@Test
	void shouldMapTaskToResponse() {
		// Arrange
		ProjectTask task = new ProjectTask("A", "Gather Requirements", 3);

		// Act
		TaskResponse response = taskApiMapper.toResponse(task);

		// Assert
		assertNull(response.id());

		assertEquals("A", response.taskKey());

		assertEquals("Gather Requirements", response.name());

		assertEquals(3, response.durationDays());

		assertTrue(response.dependencyIds().isEmpty());

		assertNull(response.calculatedStartDate());

		assertNull(response.calculatedEndDate());

		assertNull(response.version());
	}
}