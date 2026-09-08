package com.qualification.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.qualification.exam.entity.ProjectTask;

public interface ProjectTaskRepository
        extends JpaRepository<ProjectTask, Long> {

    @EntityGraph(attributePaths = "dependencies")
    List<ProjectTask> findByProjectPlanIdOrderByIdAsc(
            Long projectPlanId
    );

    boolean existsByProjectPlanIdAndTaskKey(
            Long projectPlanId,
            String taskKey
    );

    boolean existsByProjectPlanId(
            Long projectPlanId
    );
}