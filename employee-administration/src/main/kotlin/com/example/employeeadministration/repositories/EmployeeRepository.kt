package com.example.employeeadministration.repositories

import com.example.employeeadministration.model.aggregates.Employee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Defines custom database queries. Spring takes care of the implementation.
 */
@Repository
interface EmployeeRepository: JpaRepository<Employee, Long> {

    fun getAllByDeletedFalse(): List<Employee>
    fun getAllByDepartment_IdAndDeletedFalse(departmendId: Long): List<Employee>
    fun getAllByPosition_IdAndDeletedFalse(positionId: Long): List<Employee>
    fun getAllByFirstnameContainingAndLastnameContainingAndDeletedFalse(firstname: String, lastname: String): List<Employee>
    fun getByIdAndDeletedFalse(id: Long): Optional<Employee>

}