package com.example.projectadministration.services

import com.example.projectadministration.model.dto.ProjectEmployeeDto
import com.example.projectadministration.model.employee.ProjectEmployee
import com.example.projectadministration.repositories.ProjectEmployeeRepository
import org.springframework.stereotype.Service

/**
 * Service used by the ProjectEmployeeController. Allows only read access.
 */
@Service
class ProjectEmployeeService(val projectEmployeeRepository: ProjectEmployeeRepository) {

    fun mapToDto(projectEmployee: ProjectEmployee): ProjectEmployeeDto {
        return ProjectEmployeeDto(projectEmployee.employeeId, projectEmployee.firstname, projectEmployee.lastname, projectEmployee.mail, projectEmployee.department, projectEmployee.title)
    }

    fun getAllEmployees(): List<ProjectEmployeeDto> {
        return projectEmployeeRepository.findAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getEmployeeById(id: Long): ProjectEmployeeDto {
        return projectEmployeeRepository.findByEmployeeIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    fun getEmployeesByDepartment(department: String): List<ProjectEmployeeDto> {
        return projectEmployeeRepository.findByDepartmentAndDeletedFalse(department).map{ mapToDto(it) }
    }

}