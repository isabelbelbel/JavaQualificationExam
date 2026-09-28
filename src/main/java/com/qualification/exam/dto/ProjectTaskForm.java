package com.qualification.exam.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectTaskForm {

	@NotBlank(message = "Task key is required")
	@Size(max = 50, message = "Task key cannot exceed 50 characters")
	private String taskKey;

	@NotBlank(message = "Task name is required")
	@Size(max = 200, message = "Task name cannot exceed 200 characters")
	private String name;

	@Min(value = 1, message = "Duration must be at least one day")
	@Max(value = 3650, message = "Duration cannot exceed 3650 days")
	private int durationDays = 1;

	private List<Long> dependencyIds = new ArrayList<>();

	public String getTaskKey() {
		return taskKey;
	}

	public void setTaskKey(String taskKey) {
		this.taskKey = taskKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(int durationDays) {
		this.durationDays = durationDays;
	}

	public List<Long> getDependencyIds() {
		return dependencyIds;
	}

	public void setDependencyIds(List<Long> dependencyIds) {
		this.dependencyIds = dependencyIds == null ? new ArrayList<>() : new ArrayList<>(dependencyIds);
	}
}