package com.example.projectadministration.services

import com.example.projectadministration.model.aggregates.AggregateState
import com.example.projectadministration.model.aggregates.Project
import com.example.projectadministration.model.dto.*
import com.example.projectadministration.repositories.CustomerRepository
import com.example.projectadministration.repositories.ProjectRepository
import com.example.projectadministration.repositories.employee.EmployeeRepository
import com.example.projectadministration.services.employee.EmployeeService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for project interactions.
 */
@Service
class ProjectService(
        val projectRepository: ProjectRepository,
        val employeeRepository: EmployeeRepository,
        val customerRepository: CustomerRepository,
        val employeeService: EmployeeService,
        val customerService: CustomerService,
        val workflowService: WorkflowService,
        val objectMapper: ObjectMapper
): WorkflowPersistenceService<Project>, MappingService<Project, ProjectDto>, SyncMappingService<Project, ProjectSync> {

    @Transactional
    override fun saveAggregateWithWorkflow(aggregate: Project, compensationAggregate: Project?): Project {
        // The process variables. Aggregates are passed as a JSON String.
        var variablesMap = emptyMap<String, String>()
        // If the employee was created no compensation data is necessary
        if (compensationAggregate != null) {
            variablesMap = variablesMap.plus("compensationProject" to objectMapper.writeValueAsString(mapToSyncDto(compensationAggregate)))
        }
        aggregate.state = AggregateState.PENDING
        val savedProject = projectRepository.save(aggregate)
        variablesMap = variablesMap.plus("project" to objectMapper.writeValueAsString(mapToSyncDto(savedProject)))
        // Start the workflow after the aggregate was saved
        workflowService.createWorkflowInstance(variablesMap, "synchronize-project")
        return savedProject
    }

    override fun mapToSyncDto(project: Project): ProjectSync {
        return ProjectSync(
                project.id!!,
                project.name,
                project.description,
                project.startDate,
                project.projectedEndDate,
                project.endDate,
                project.employees.map { it.employeeId }.toMutableSet(),
                project.customer.id!!,
                project.deleted,
                project.state
        )
    }

    override fun mapToDto(project: Project): ProjectDto {
        val employeeDtos = project.employees.map { ProjectEmployeeDto(it.employeeId, it.firstname, it.lastname, it.companyMail, it.state) }.toMutableSet()
        val customerDto = ProjectCustomerDto(project.customer.id!!, project.customer.customerName, project.customer.state)
        return ProjectDto(project.id, project.name, project.description, project.startDate, project.projectedEndDate, project.endDate, employeeDtos, customerDto, project.state)

    }

    override fun mapToEntity(dto: ProjectDto): Project {
        val employees = employeeRepository.findAllByEmployeeIdInAndDeletedFalse(dto.projectEmployees.map { it.id }).toSet()
        val customer = customerRepository.findById(dto.customer.id).orElseThrow()
        return Project(dto.id, dto.name, dto.description, dto.startDate, dto.projectedEndDate, dto.endDate, employees.toMutableSet(), customer)
    }

    @Transactional
    fun getAllProjects(): List<ProjectDto> {
        return projectRepository.getAllByDeletedFalse().map { mapToDto(it) }
    }

    @Transactional
    fun getProjectById(id: Long): ProjectDto {
        return projectRepository.getByIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    @Transactional
    fun getFinishedProjects(): List<ProjectDto> {
        return projectRepository.getAllByEndDateNotNullAndDeletedFalse().map { mapToDto(it) }
    }

    @Transactional
    fun getProjectsOfEmployee(employeeId: Long): List<ProjectDto> {
        return employeeRepository.findByEmployeeIdAndDeletedFalse(employeeId).map { projectEmployee ->
            projectRepository.getAllByEmployeesContainingAndDeletedFalse(projectEmployee).map { mapToDto(it) }
        }.orElseThrow()
    }

    @Transactional
    fun getProjectsOfCustomer(customerId: Long): List<ProjectDto> {
        return projectRepository.getAllByCustomerIdAndDeletedFalse(customerId).map { mapToDto(it) }
    }

    @Transactional
    fun createProject(projectDto: ProjectDto): ProjectDto {
        return mapToDto(saveAggregateWithWorkflow(mapToEntity(projectDto), null))
    }

    /**
     * Compares received project data with local project data and applies updates.
     * The service does not just set the fields, it uses the aggregate functions to do so because these functions can contain business rules which have to be applied.
     * It is important to note that no updates are permitted without using aggregate functions.
     */
    @Transactional
    fun updateProject(projectDto: ProjectDto): ProjectDto {
        val project = projectRepository.getByIdAndDeletedFalse(projectDto.id!!).orElseThrow()
        if (project.state == AggregateState.PENDING) throw RuntimeException("Project is still pending")
        val compensationProject = project.copy()
        if (project.description != projectDto.description) {
            project.updateProjectDescription(projectDto.description)
        }
        if (project.projectedEndDate != projectDto.projectedEndDate) {
            project.delayProject(projectDto.projectedEndDate)
        }
        if (project.endDate != projectDto.endDate) {
            project.finishProject(projectDto.endDate!!)
        }
        projectDto.projectEmployees.map { it.id }.filter {id ->  !project.employees.map { it.employeeId }.contains(id) }.forEach {
            project.addEmployeeToProject(employeeRepository.findByEmployeeIdAndDeletedFalse(it).orElseThrow())
        }
        project.employees.map { it.employeeId }.filter {id ->  !projectDto.projectEmployees.map { it.id }.contains(id) }.forEach {
            project.removeEmployeeFromProject(employeeRepository.findByEmployeeIdAndDeletedFalse(it!!).orElseThrow())
        }
        return mapToDto(saveAggregateWithWorkflow(project, compensationProject))
    }

    @Transactional
    fun deleteProject(id: Long): ProjectDto {
        val project = projectRepository.getByIdAndDeletedFalse(id).orElseThrow()
        if (project.state == AggregateState.PENDING) throw RuntimeException("Project is still pending")
        val compensationProject = project.copy()
        project.deleteAggregate()
        return mapToDto(saveAggregateWithWorkflow(project, compensationProject))
    }



}