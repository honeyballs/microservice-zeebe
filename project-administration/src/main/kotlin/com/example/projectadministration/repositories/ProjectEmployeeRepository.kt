package com.example.projectadministration.repositories

import com.example.projectadministration.model.employee.ProjectEmployee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProjectEmployeeRepository: JpaRepository<ProjectEmployee, Long> {

    fun findAllByDeletedFalse(): List<ProjectEmployee>
    fun findByEmployeeIdAndDeletedFalse(id: Long): Optional<ProjectEmployee>
    fun findByEmployeeId(id: Long): Optional<ProjectEmployee>
    fun findByDepartmentAndDeletedFalse(department: String): List<ProjectEmployee>

    fun findAllByEmployeeIdInAndDeletedFalse(ids: MutableSet<Long>): List<ProjectEmployee>

}