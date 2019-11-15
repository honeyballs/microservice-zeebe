package com.example.worktimeadministration.repositories

import com.example.worktimeadministration.model.employee.WorktimeEmployee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WorktimeEmployeeRepository: JpaRepository<WorktimeEmployee, Long> {

    fun deleteByEmployeeId(id: Long)

    fun findByEmployeeId(id: Long): Optional<WorktimeEmployee>
    fun findAllByDeletedFalse(): List<WorktimeEmployee>
    fun findByEmployeeIdAndDeletedFalse(id: Long): Optional<WorktimeEmployee>
    fun findAllByEmployeeIdIn(ids: Set<Long>): List<WorktimeEmployee>
}