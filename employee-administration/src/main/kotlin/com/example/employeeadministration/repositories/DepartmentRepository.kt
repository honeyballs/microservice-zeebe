package com.example.employeeadministration.repositories

import com.example.employeeadministration.model.aggregates.Department
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional(readOnly = true)
interface DepartmentRepository: JpaRepository<Department, Long> {

    fun getAllByDeletedFalse(): List<Department>
    fun getByIdAndDeletedFalse(id: Long): Optional<Department>
    fun getByNameAndDeletedFalse(name: String): Optional<Department>

}