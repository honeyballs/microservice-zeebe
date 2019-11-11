package com.example.projectadministration.repositories

import com.example.projectadministration.model.Project
import com.example.projectadministration.model.employee.ProjectEmployee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface ProjectRepository: JpaRepository<Project, Long> {

    fun findAllByDeletedFalse(): List<Project>
    fun findByIdAndDeletedFalse(id: Long): Optional<Project>
    fun findAllByEmployeesContainingAndDeletedFalse(employee: ProjectEmployee): List<Project>
    fun findAllByEndDateNotNullAndDeletedFalse(): List<Project>

}