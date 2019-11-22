package com.example.projectadministration.services

import com.example.projectadministration.model.AggregateState
import com.example.projectadministration.model.Project
import com.example.projectadministration.model.dto.ProjectDto
import com.example.projectadministration.model.dto.ProjectSyncDto
import com.example.projectadministration.repositories.ProjectEmployeeRepository
import com.example.projectadministration.repositories.ProjectRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

/**
 * Service for project interactions.
 */
@Service
class ProjectService(
        val projectRepository: ProjectRepository,
        val employeeService: ProjectEmployeeService,
        val employeeRepository: ProjectEmployeeRepository,
        val workflowService: WorkflowService,
        val objectMapper: ObjectMapper
) {

    /**
     * Saves a project and starts the replication workflow.
     */
    fun saveProjectWithWorkflow(project: Project, compensationProject: Project?): Project {
        // The process variables. Aggregates are passed as a JSON String.
        var variablesMap = emptyMap<String, String>()
        // If the employee was created no compensation data is necessary
        if (compensationProject != null) {
            variablesMap = variablesMap.plus("compensationProject" to objectMapper.writeValueAsString(mapToSyncDto(compensationProject)))
        }
        project.state = AggregateState.PENDING
        val savedProject = projectRepository.save(project)
        variablesMap = variablesMap.plus("project" to objectMapper.writeValueAsString(mapToSyncDto(savedProject)))
        // Start the workflow after the aggregate was saved
        workflowService.createWorkflowInstance(variablesMap, "synchronize-project")
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
        return mapToDto(saveProjectWithWorkflow(project, null))
    }

    /**
     * Compares received project data with local project data and applies updates.
     * The service does not just set the fields, it uses the aggregate functions to do so because these functions can contain business rules which have to be applied.
     * It is important to note that no updates are permitted without using aggregate functions.
     */
    fun updateProject(projectDto: ProjectDto): ProjectDto {
        val project = projectRepository.findByIdAndDeletedFalse(projectDto.id!!).orElseThrow()
        val compensationProject = project.copy()
        if (project.endDate != projectDto.endDate) {
            project.finishProject(projectDto.endDate!!)
        }
        projectDto.employees.map { it.id }.filter {id ->  !project.employees.map { it.employeeId }.contains(id) }.forEach {
            project.addEmployeeToProject(employeeRepository.findByEmployeeIdAndDeletedFalse(it).orElseThrow())
        }
        project.employees.map { it.id }.filter {id ->  !projectDto.employees.map { it.id }.contains(id) }.forEach {
            project.removeEmployeeFromProject(employeeRepository.findByEmployeeIdAndDeletedFalse(it!!).orElseThrow())
        }
        return mapToDto(saveProjectWithWorkflow(project, compensationProject))
    }

    fun deleteProject(id: Long): ProjectDto {
        val project = projectRepository.findByIdAndDeletedFalse(id).orElseThrow()
        val compensationProject = project.copy()
        project.deleteAggregate()
        return mapToDto(saveProjectWithWorkflow(project, compensationProject))
    }

}