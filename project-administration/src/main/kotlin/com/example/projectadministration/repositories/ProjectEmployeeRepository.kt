package com.example.projectadministration.repositories

import com.example.projectadministration.model.employee.ProjectEmployee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Defines custom database queries. Spring takes care of the implementation.
 */
@Repository
interface ProjectEmployeeRepository: JpaRepository<ProjectEmployee, Long> {

    fun deleteByEmployeeId(id: Long)

    fun findAllByDeletedFalse(): List<ProjectEmployee>
    fun findByEmployeeIdAndDeletedFalse(id: Long): Optional<ProjectEmployee>
    fun findByEmployeeId(id: Long): Optional<ProjectEmployee>
    fun findByDepartmentAndDeletedFalse(department: String): List<ProjectEmployee>

    fun findAllByEmployeeIdInAndDeletedFalse(ids: MutableSet<Long>): List<ProjectEmployee>

}