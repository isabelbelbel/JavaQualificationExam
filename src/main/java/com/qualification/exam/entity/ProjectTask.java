package com.qualification.exam.entity;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "project_task", uniqueConstraints = {
		@UniqueConstraint(name = "uk_project_task_key", columnNames = { "project_plan_id", "task_key" }) })
public class ProjectTask {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "task_key", nullable = false, length = 50)
	private String taskKey;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(name = "duration_days", nullable = false)
	private int durationDays;

	@Column(name = "calculated_start_date")
	private LocalDate calculatedStartDate;

	@Column(name = "calculated_end_date")
	private LocalDate calculatedEndDate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_plan_id", nullable = false)
	private ProjectPlan projectPlan;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "task_dependency", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "dependency_task_id"))
	private Set<ProjectTask> dependencies = new LinkedHashSet<>();

	@Version
	private Long version;

	protected ProjectTask() {
		// Required by JPA.
	}

	public ProjectTask(String taskKey, String name, int durationDays) {
		this.taskKey = taskKey;
		this.name = name;
		this.durationDays = durationDays;
	}

	public Long getId() {
		return id;
	}

	public String getTaskKey() {
		return taskKey;
	}

	public String getName() {
		return name;
	}

	public int getDurationDays() {
		return durationDays;
	}

	public LocalDate getCalculatedStartDate() {
		return calculatedStartDate;
	}

	public LocalDate getCalculatedEndDate() {
		return calculatedEndDate;
	}

	public ProjectPlan getProjectPlan() {
		return projectPlan;
	}

	public Set<ProjectTask> getDependencies() {
		return Set.copyOf(dependencies);
	}

	public Long getVersion() {
		return version;
	}

	public void setTaskKey(String taskKey) {
		this.taskKey = taskKey;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDurationDays(int durationDays) {
		this.durationDays = durationDays;
	}

	public void assignToProject(ProjectPlan projectPlan) {
		this.projectPlan = projectPlan;
	}

	public void addDependency(ProjectTask dependency) {
		dependencies.add(dependency);
	}

	public void removeDependency(ProjectTask dependency) {
		dependencies.remove(dependency);
	}

	public void clearDependencies() {
		dependencies.clear();
	}

	public void updateSchedule(LocalDate startDate, LocalDate endDate) {
		this.calculatedStartDate = startDate;
		this.calculatedEndDate = endDate;
	}

	public void clearSchedule() {
		this.calculatedStartDate = null;
		this.calculatedEndDate = null;
	}
}