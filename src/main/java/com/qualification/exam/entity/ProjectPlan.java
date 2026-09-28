package com.qualification.exam.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "project_plan")
public class ProjectPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "calculated_end_date")
	private LocalDate calculatedEndDate;

	@Version
	private Long version;

	@OneToMany(mappedBy = "projectPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("id ASC")
	private List<ProjectTask> tasks = new ArrayList<>();

	protected ProjectPlan() {
		// Required by JPA.
	}

	public ProjectPlan(String name, LocalDate startDate) {
		this.name = name;
		this.startDate = startDate;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getCalculatedEndDate() {
		return calculatedEndDate;
	}

	public Long getVersion() {
		return version;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public void setCalculatedEndDate(LocalDate calculatedEndDate) {
		this.calculatedEndDate = calculatedEndDate;
	}

	public List<ProjectTask> getTasks() {
		return Collections.unmodifiableList(tasks);
	}

	public void addTask(ProjectTask task) {
		tasks.add(task);
		task.assignToProject(this);
	}

	public void removeTask(ProjectTask task) {
		tasks.remove(task);
		task.assignToProject(null);
	}
}