package com.example.worktimeadministration.services.project

import com.example.worktimeadministration.model.dto.ProjectDto
import com.example.worktimeadministration.model.project.Project
import com.example.worktimeadministration.repositories.employee.EmployeeRepository
import com.example.worktimeadministration.repositories.project.ProjectRepository
import com.example.worktimeadministration.services.MappingService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectService(
        val projectRepository: ProjectRepository,
        val employeeRepository: EmployeeRepository
): MappingService<Project, ProjectDto> {

    override fun mapToDto(aggregate: Project): ProjectDto {
        return ProjectDto(
                aggregate.projectId,
                aggregate.name,
                aggregate.description,
                aggregate.startDate,
                aggregate.projectedEndDate,
                aggregate.endDate,
                aggregate.employees.map { it.employeeId }.toSet(),
                aggregate.state
        )
    }

    override fun mapToEntity(dto: ProjectDto): Project {
        TODO("not required") //To change body of created functions use File | Settings | File Templates.
    }

    @Transactional
    fun getAllProjects(): List<ProjectDto> {
        return projectRepository.findAllByDeletedFalse().map { mapToDto(it) }
    }

    @Transactional
    fun getProjectById(id: Long): ProjectDto {
        return projectRepository.findByProjectIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    @Transactional
    fun getProjectsOfEmployee(employeeId: Long): List<ProjectDto> {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId).map {
            projectRepository.findAllByEmployeesContainsAndDeletedFalse(it).map {p -> mapToDto(p) }
        }.orElseThrow()
    }
}