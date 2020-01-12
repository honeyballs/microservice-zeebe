package com.example.worktimeadministration.repositories

import com.example.worktimeadministration.model.aggregates.WorktimeEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Defines custom database queries. Spring takes care of the implementation.
 */
@Repository
interface WorktimeEntryRepository: JpaRepository<WorktimeEntry, Long> {


    fun findAllByDeletedFalse(): List<WorktimeEntry>
    fun findByIdAndDeletedFalse(id: Long): Optional<WorktimeEntry>
    fun findAllByProjectProjectIdAndDeletedFalse(id: Long): List<WorktimeEntry>
    fun findAllByEmployeeEmployeeIdAndDeletedFalse(id: Long): List<WorktimeEntry>
    fun findAllByProjectProjectIdAndEmployeeEmployeeIdAndDeletedFalse(projectId: Long, employeeId: Long): List<WorktimeEntry>


}