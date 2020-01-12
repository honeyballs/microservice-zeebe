package com.example.projectadministration.services.employee

import com.example.projectadministration.model.dto.EmployeeDto
import com.example.projectadministration.model.employee.Employee
import com.example.projectadministration.repositories.ProjectRepository
import com.example.projectadministration.repositories.employee.EmployeeRepository
import com.example.projectadministration.services.MappingService
import org.springframework.stereotype.Service

@Service
class EmployeeService(
        val employeeRepository: EmployeeRepository,
        val projectRepository: ProjectRepository
): MappingService<Employee, EmployeeDto> {
    
    override fun mapToDto(aggregate: Employee): EmployeeDto {
        return EmployeeDto(aggregate.employeeId, aggregate.firstname, aggregate.lastname, aggregate.department.name, aggregate.position.title, aggregate.companyMail, aggregate.state)
    }

    override fun mapToEntity(dto: EmployeeDto): Employee {
        TODO("Not required")
    }

    fun getAllEmployees(): List<EmployeeDto> {
        return employeeRepository.findAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getEmployeeById(id: Long): EmployeeDto {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    fun getAllEmployeesInDepartment(name: String): List<EmployeeDto> {
        return employeeRepository.findAllByDepartmentNameAndDeletedFalse(name).map { mapToDto(it) }
    }

    fun getAllEmployeesInPosition(title: String): List<EmployeeDto> {
        return employeeRepository.findAllByPositionTitleAndDeletedFalse(title).map { mapToDto(it) }
    }

    fun getAllEmployeesOfProject(projectId: Long): List<EmployeeDto> {
        return projectRepository.getByIdAndDeletedFalse(projectId).map {
            it.employees.map {e -> mapToDto(e) }
        }.orElseThrow()
    }
}