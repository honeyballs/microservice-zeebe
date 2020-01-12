package com.example.worktimeadministration.services.employee

import com.example.worktimeadministration.model.dto.EmployeeDto
import com.example.worktimeadministration.model.employee.Employee
import com.example.worktimeadministration.repositories.employee.EmployeeRepository
import com.example.worktimeadministration.repositories.project.ProjectRepository
import com.example.worktimeadministration.services.MappingService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeService(
        val employeeRepository: EmployeeRepository,
        val projectRepository: ProjectRepository
): MappingService<Employee, EmployeeDto> {

    override fun mapToDto(aggregate: Employee): EmployeeDto {
        return EmployeeDto(
                aggregate.employeeId,
                aggregate.firstname,
                aggregate.lastname,
                aggregate.state
        )
    }

    override fun mapToEntity(dto: EmployeeDto): Employee {
        TODO("Not needed") //To change body of created functions use File | Settings | File Templates.
    }

    fun getAllEmployees(): List<EmployeeDto> {
        return employeeRepository.findAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getEmployeeById(id: Long): EmployeeDto {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    @Transactional
    fun getEmployeesOfProject(projectId: Long): List<EmployeeDto> {
        return projectRepository.findByProjectIdAndDeletedFalse(projectId).map { it.employees.map { mapToDto(it) } }.orElseThrow()
    }
}