package com.example.worktimeadministration.services

import com.example.worktimeadministration.model.dto.WorktimeEmployeeDto
import com.example.worktimeadministration.model.employee.WorktimeEmployee
import com.example.worktimeadministration.repositories.WorktimeEmployeeRepository
import com.example.worktimeadministration.repositories.WorktimeProjectRepository
import org.springframework.stereotype.Service

/**
 * Service used by the WorktimeEmployeeController. Allows only read access.
 */
@Service
class WorktimeEmployeeService(
        val employeeRepository: WorktimeEmployeeRepository,
        val projectRepository: WorktimeProjectRepository,
        val projectService: WorktimeProjectService
): MappingService<WorktimeEmployee, WorktimeEmployeeDto> {

    override fun mapToDto(employee: WorktimeEmployee): WorktimeEmployeeDto {
        val projects = projectRepository.findAllByEmployeesContaining(employee).map { projectService.mapToDto(it) }.toMutableSet()
        return WorktimeEmployeeDto(employee.employeeId, employee.firstname, employee.lastname, projects, employee.state)
    }

    fun getAllEmployees(): List<WorktimeEmployeeDto> {
        return employeeRepository.findAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getById(id: Long): WorktimeEmployeeDto {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

}