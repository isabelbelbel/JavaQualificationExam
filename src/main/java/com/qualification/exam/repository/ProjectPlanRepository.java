package com.qualification.exam.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.qualification.exam.entity.ProjectPlan;

public interface ProjectPlanRepository
        extends JpaRepository<ProjectPlan, Long> {

    @EntityGraph(attributePaths = "tasks")
    Optional<ProjectPlan> findWithTasksById(Long id);
}