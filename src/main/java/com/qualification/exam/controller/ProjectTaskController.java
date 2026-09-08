package com.qualification.exam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.qualification.exam.dto.ProjectTaskForm;
import com.qualification.exam.exception.InvalidProjectPlanException;
import com.qualification.exam.service.ProjectPlanService;
import com.qualification.exam.service.ProjectTaskService;
import com.qualification.exam.service.ProjectTaskApplicationService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/projects/{projectId}/tasks")
public class ProjectTaskController {

    private final ProjectPlanService projectPlanService;
    private final ProjectTaskService projectTaskService;
    private final ProjectTaskApplicationService projectTaskApplicationService;

    public ProjectTaskController(
            ProjectPlanService projectPlanService,
            ProjectTaskService projectTaskService,
            ProjectTaskApplicationService
                    projectTaskApplicationService
    ) {
        this.projectPlanService = projectPlanService;
        this.projectTaskService = projectTaskService;
        this.projectTaskApplicationService =
                projectTaskApplicationService;
    }

    @GetMapping("/new")
    public String showCreateForm(
    		@PathVariable("projectId") Long projectId,
            Model model
    ) {
        model.addAttribute(
                "projectTaskForm",
                new ProjectTaskForm()
        );

        prepareFormModel(projectId, model);

        return "tasks/form";
    }

    @PostMapping
    public String createTask(
    		@PathVariable("projectId") Long projectId,
            @Valid ProjectTaskForm projectTaskForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(projectId, model);
            return "tasks/form";
        }

        try {
            projectTaskApplicationService.createAndRecalculate(
                    projectId,
                    projectTaskForm
            );
        } catch (InvalidProjectPlanException exception) {
            bindingResult.reject(
                    "task.invalid",
                    exception.getMessage()
            );

            prepareFormModel(projectId, model);

            return "tasks/form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Task created and schedule recalculated successfully"
        );

        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/{taskId}/delete")
    public String deleteTask(
            @PathVariable("projectId") Long projectId,
            @PathVariable("taskId") Long taskId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            projectTaskApplicationService
                    .deleteAndRecalculate(
                            projectId,
                            taskId
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Task deleted and schedule recalculated successfully"
            );
        } catch (InvalidProjectPlanException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/projects/" + projectId;
    }
    private void prepareFormModel(
            Long projectId,
            Model model
    ) {
        model.addAttribute(
                "project",
                projectPlanService.findById(projectId)
        );

        model.addAttribute(
                "availableDependencies",
                projectTaskService.findByProjectId(projectId)
        );
    }
}