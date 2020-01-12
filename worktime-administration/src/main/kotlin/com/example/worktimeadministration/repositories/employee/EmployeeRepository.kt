package com.example.worktimeadministration.repositories.employee

import com.example.worktimeadministration.model.employee.Employee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EmployeeRepository: JpaRepository<Employee, Long> {

    fun deleteByEmployeeId(id: Long)
    fun findByEmployeeId(id: Long): Optional<Employee>
    fun findAllByEmployeeIdIn(ids: List<Long>): List<Employee>

    fun findAllByDeletedFalse(): List<Employee>
    fun findByEmployeeIdAndDeletedFalse(id: Long): Optional<Employee>

}