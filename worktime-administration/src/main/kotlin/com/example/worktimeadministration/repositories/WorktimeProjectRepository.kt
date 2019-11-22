package com.example.worktimeadministration.repositories

import com.example.worktimeadministration.model.employee.WorktimeEmployee
import com.example.worktimeadministration.model.project.WorktimeProject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Defines custom database queries. Spring takes care of the implementation.
 */
@Repository
interface WorktimeProjectRepository: JpaRepository<WorktimeProject, Long> {

    fun deleteByProjectId(id: Long)

    fun findByProjectId(id: Long): Optional<WorktimeProject>
    fun findAllByDeletedFalse(): List<WorktimeProject>
    fun findByProjectIdAndDeletedFalse(id: Long): Optional<WorktimeProject>
    fun findByEndDateNullAndDeletedFalse(): List<WorktimeProject>
    fun findAllByEmployeesContaining(employee: WorktimeEmployee): List<WorktimeProject>

}