package com.example.worktimeadministration.repositories.project

import com.example.worktimeadministration.model.employee.Employee
import com.example.worktimeadministration.model.project.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProjectRepository: JpaRepository<Project, Long> {

    fun findByProjectId(id: Long): Optional<Project>

    fun findAllByDeletedFalse(): List<Project>
    fun findByProjectIdAndDeletedFalse(id: Long): Optional<Project>
    fun findAllByEmployeesContainsAndDeletedFalse(employee: Employee): List<Project>
    fun findAllByProjectIdInAndDeletedFalse(ids: List<Long>): List<Project>
    fun deleteByProjectId(id: Long)
}