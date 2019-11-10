package com.example.employeeadministration.repositories

import com.example.employeeadministration.model.Employee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EmployeeRepository: JpaRepository<Employee, Long> {

    fun findByIdAndDeletedFalse(id: Long): Optional<Employee>
    fun findAllByDeletedFalse(): List<Employee>
    fun findByDepartmentAndDeletedFalse(department: String): List<Employee>

}