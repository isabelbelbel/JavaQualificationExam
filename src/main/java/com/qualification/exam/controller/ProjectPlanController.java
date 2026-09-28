package com.qualification.exam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.qualification.exam.dto.ProjectPlanForm;
import com.qualification.exam.entity.ProjectPlan;
import com.qualification.exam.service.ProjectPlanService;
import com.qualification.exam.service.ProjectTaskService;

import jakarta.validation.Valid;

import com.qualification.exam.exception.InvalidProjectPlanException;
import com.qualification.exam.service.ProjectSchedulingService;
import com.qualification.exam.exception.InvalidProjectPlanException;
import com.qualification.exam.service.AuditLogService;

@Controller
@RequestMapping("/projects")
public class ProjectPlanController {

	private final ProjectPlanService projectPlanService;
	private final ProjectTaskService projectTaskService;
	private final AuditLogService auditLogService;

	public ProjectPlanController(ProjectPlanService projectPlanService, ProjectTaskService projectTaskService
			, AuditLogService auditLogService)
	{
		this.projectPlanService = projectPlanService;
		this.projectTaskService = projectTaskService;
		this.auditLogService = auditLogService;
	}

	@GetMapping
	public String showProjectList(Model model) {
		model.addAttribute("projects", projectPlanService.findAll());

		return "projects/list";
	}

	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("projectPlanForm", new ProjectPlanForm());

		return "projects/form";
	}

	@PostMapping
	public String createProject(@Valid ProjectPlanForm projectPlanForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "projects/form";
		}

		ProjectPlan savedProject = projectPlanService.create(projectPlanForm);

		redirectAttributes.addFlashAttribute("successMessage", "Project created successfully");

		return "redirect:/projects/" + savedProject.getId();
	}

	@GetMapping("/{id}")
	public String showProjectDetails(@PathVariable Long id, Model model) {
		model.addAttribute("project", projectPlanService.findById(id));

		model.addAttribute("tasks", projectTaskService.findByProjectId(id));

		return "projects/details";
	}

	@PostMapping("/{id}/delete")
	public String deleteProject(@PathVariable("id") Long projectId, RedirectAttributes redirectAttributes) {
		try {
			projectPlanService.delete(projectId);

			redirectAttributes.addFlashAttribute("successMessage", "Project deleted successfully");
		} catch (InvalidProjectPlanException exception) {
		    auditLogService.recordFailure(
		            "DELETE_PROJECT",
		            projectId,
		            null,
		            exception.getClass().getSimpleName(),
		            exception.getMessage()
		    );

		    redirectAttributes.addFlashAttribute(
		            "errorMessage",
		            exception.getMessage()
		    );
		}

		return "redirect:/projects";
	}

}