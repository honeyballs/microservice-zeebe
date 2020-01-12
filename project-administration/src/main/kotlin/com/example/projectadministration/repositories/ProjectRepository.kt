package com.example.projectadministration.repositories

import com.example.projectadministration.model.aggregates.Project
import com.example.projectadministration.model.employee.Employee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Defines custom database queries. Spring takes care of the implementation.
 */
@Repository
interface ProjectRepository: JpaRepository<Project, Long> {

    fun getAllByDeletedFalse(): List<Project>
    fun getByIdAndDeletedFalse(id: Long): Optional<Project>
    fun getAllByCustomerIdAndDeletedFalse(id: Long): List<Project>
    fun getAllByEmployeesContainingAndDeletedFalse(employee: Employee): List<Project>
    fun getAllByEndDateNotNullAndDeletedFalse(): List<Project>
    fun getAllByEndDateNullAndDeletedFalse(): List<Project>

}