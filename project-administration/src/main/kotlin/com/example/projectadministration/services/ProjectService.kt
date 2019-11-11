package com.example.projectadministration.services

import com.example.projectadministration.model.AggregateState
import com.example.projectadministration.model.Project
import com.example.projectadministration.model.dto.ProjectDto
import com.example.projectadministration.model.dto.ProjectSyncDto
import com.example.projectadministration.repositories.ProjectEmployeeRepository
import com.example.projectadministration.repositories.ProjectRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class ProjectService(
        val projectRepository: ProjectRepository,
        val employeeService: ProjectEmployeeService,
        val employeeRepository: ProjectEmployeeRepository,
        val workflowService: WorkflowService,
        val objectMapper: ObjectMapper
) {

    fun saveProjectWithWorkflow(project: Project, operation: Operation): Project {
        project.state = AggregateState.PENDING
        val savedProject = projectRepository.save(project)
        val pair = "project" to objectMapper.writeValueAsString(mapToSyncDto(savedProject))
        workflowService.createWorkflowInstance(mapOf(pair), "project-${operation.value}")
        return savedProject
    }

    fun mapToSyncDto(project: Project): ProjectSyncDto {
        return ProjectSyncDto(
                project.id!!,
                project.name,
                project.customer,
                project.startDate,
                project.endDate,
                project.employees.map { it.employeeId }.toMutableSet(),
                project.deleted,
                project.state
        )
    }

    fun mapToDto(project: Project): ProjectDto {
        return ProjectDto(
                project.id!!,
                project.name,
                project.customer,
                project.startDate,
                project.endDate,
                project.employees.map { employeeService.mapToDto(it) }.toMutableSet(),
                project.state
        )
    }

    fun getAllProjects(): List<ProjectDto> {
        return projectRepository.findAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getProjectById(id: Long): ProjectDto {
        return projectRepository.findByIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    fun getFinishedProjects(): List<ProjectDto> {
        return projectRepository.findAllByEndDateNotNullAndDeletedFalse().map { mapToDto(it) }
    }

    fun getProjectsOfEmployee(employeeId: Long): List<ProjectDto> {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId).map { projectEmployee ->
            projectRepository.findAllByEmployeesContainingAndDeletedFalse(projectEmployee).map { mapToDto(it) }
        }.orElseThrow()
    }

    fun createProject(projectDto: ProjectDto): ProjectDto {
        val employees = employeeRepository.findAllByEmployeeIdInAndDeletedFalse(projectDto.employees.map { it.id }.toMutableSet()).toMutableSet()
        val project = Project(null, projectDto.name, projectDto.customer, projectDto.startDate, projectDto.endDate, employees)
        return mapToDto(saveProjectWithWorkflow(project, Operation.CREATED))
    }

    fun updateProject(projectDto: ProjectDto): ProjectDto {
        val project = projectRepository.findByIdAndDeletedFalse(projectDto.id).orElseThrow()
        if (project.endDate != projectDto.endDate) {
            project.finishProject(projectDto.endDate!!)
        }
        projectDto.employees.map { it.id }.filter {id ->  !project.employees.map { it.employeeId }.contains(id) }.forEach {
            project.addEmployeeToProject(employeeRepository.findByEmployeeIdAndDeletedFalse(it).orElseThrow())
        }
        project.employees.map { it.id }.filter {id ->  !projectDto.employees.map { it.id }.contains(id) }.forEach {
            project.removeEmployeeFromProject(employeeRepository.findByEmployeeIdAndDeletedFalse(it!!).orElseThrow())
        }
        return mapToDto(saveProjectWithWorkflow(project, Operation.UPDATED))
    }

    fun deleteProject(id: Long): ProjectDto {
        val project = projectRepository.findByIdAndDeletedFalse(id).orElseThrow()
        project.deleteAggregate()
        return mapToDto(saveProjectWithWorkflow(project, Operation.DELETED))
    }

}

enum class Operation(val value: String) {
    CREATED("created"), UPDATED("updated"), DELETED("deleted")
}