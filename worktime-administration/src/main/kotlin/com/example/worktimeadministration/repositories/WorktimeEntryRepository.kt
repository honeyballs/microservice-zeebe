package com.example.worktimeadministration.repositories

import com.example.worktimeadministration.model.WorktimeEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WorktimeEntryRepository: JpaRepository<WorktimeEntry, Long> {

    fun findAllByDeletedFalse(): List<WorktimeEntry>
    fun findByIdAndDeletedFalse(id: Long): Optional<WorktimeEntry>
    fun findAllByEmployee_EmployeeIdAndDeletedFalse(employeeId: Long): List<WorktimeEntry>
    fun findAllByProject_ProjectIdAndDeletedFalse(projectId: Long): List<WorktimeEntry>

}