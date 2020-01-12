package com.example.worktimeadministration.jobhandlers

import com.example.worktimeadministration.model.aggregates.AggregateState
import com.example.worktimeadministration.model.employee.Employee
import com.example.worktimeadministration.model.project.Project
import com.example.worktimeadministration.model.project.ProjectSync
import com.example.worktimeadministration.repositories.employee.EmployeeRepository
import com.example.worktimeadministration.repositories.project.ProjectRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.UnexpectedRollbackException
import org.springframework.transaction.annotation.Transactional

/**
 * This class contains the functions used by the Zeebe JobHandlers to execute logic when a job is received.
 * The functions are stored in variables, so they can be passed to the handlers.
 *
 * A singleton instance of this class is created when the application context is initialized.
 * This way a repository and the ObjectMapper (used for JSON Deserialization) can be injected into the class to use them in these functions.
 *
 */
@Component
class ProjectJobHandlers(
        val projectSyncTransactionService: ProjectSyncTransactionService,
        val mapper: ObjectMapper
) {

    val synchronizeProjects: (JobClient, ActivatedJob) -> Unit = { jobClient, job ->
        println("EXECUTING SYNC JOB")
        val projectDto = mapper.readValue<ProjectSync>(job.variablesAsMap["project"] as String)
        try {
            projectSyncTransactionService.handleSynchronization(projectDto)
            val result = mapOf("worktimeProjectSynced" to true)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        } catch (e: Exception) {
            e.printStackTrace()
            val result = mapOf("worktimeProjectSynced" to false)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        }
    }

    val activateProject: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING ACTIVATION JOB")
        val projectId = mapper.readValue<ProjectSync>(job.variablesAsMap["project"] as String).id
        projectSyncTransactionService.handleActivation(projectId)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensateProject: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val projectId = mapper.readValue<ProjectSync>(job.variablesAsMap["project"] as String).id
        if (variables.containsKey("compensationProject")) {
            projectSyncTransactionService.handleCompensation(projectId, mapper.readValue<ProjectSync>(job.variablesAsMap["compensationProject"] as String))
        } else {
            projectSyncTransactionService.handleCompensation(projectId, null)
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

}

@Service
class ProjectSyncTransactionService(
        val projectRepository: ProjectRepository,
        val employeeRepository: EmployeeRepository
): SyncService<ProjectSync> {

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleSynchronization(projectDto: ProjectSync) {
        // Retrieve and update the employee or create it if it doesn't exist
        projectRepository.findByProjectId(projectDto.id).map { project ->
            project.endDate = projectDto.endDate
            project.description = projectDto.description
            project.projectedEndDate = projectDto.projectedEndDate
            project.deleted = projectDto.deleted
            project.state = projectDto.state
            // Add employees to project if necessary
            projectDto.employees.filter {id ->  !project.employees.map { it.employeeId }.contains(id) }.forEach {
                project.employees.add(employeeRepository.findByEmployeeIdAndDeletedFalse(it).orElseThrow())
            }
            // Remove employees from project if necessary
            project.employees.map { it.employeeId }.filter {id ->  !projectDto.employees.map { it }.contains(id) }.forEach {
                project.employees.removeIf {t: Employee -> t.employeeId == it }
            }
            projectRepository.save(project)
        }.orElseGet {
            val employees = employeeRepository.findAllByEmployeeIdIn(projectDto.employees.toList()).toMutableSet()
            val worktimeProject = Project(
                    null,
                    projectDto.id,
                    projectDto.name,
                    projectDto.description,
                    projectDto.startDate,
                    projectDto.projectedEndDate,
                    projectDto.endDate,
                    employees.toMutableSet(),
                    projectDto.deleted,
                    projectDto.state
            )
            projectRepository.save(worktimeProject)
        }
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleActivation(projectId: Long) {
        val project = projectRepository.findByProjectId(projectId).orElseThrow()
        project.state = AggregateState.ACTIVE
        projectRepository.save(project)
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleCompensation(projectId: Long, compensationProject: ProjectSync?) {
        if (compensationProject != null) {
            projectRepository.findByProjectId(projectId).ifPresent { project ->
                project.endDate = compensationProject.endDate
                project.description = compensationProject.description
                project.projectedEndDate = compensationProject.projectedEndDate
                project.deleted = compensationProject.deleted
                // Add employees to project if necessary
                compensationProject.employees.filter {id ->  !project.employees.map { it.employeeId }.contains(id) }.forEach {
                    project.employees.add(employeeRepository.findByEmployeeId(it).orElseThrow())
                }
                // Remove employees from project if necessary
                project.employees.map { it.employeeId }.filter {id ->  !compensationProject.employees.map { it }.contains(id) }.forEach {
                    project.employees.removeIf {t: Employee -> t.employeeId == it }
                }
                project.state = AggregateState.ACTIVE
                projectRepository.save(project)
            }
        } else {
            projectRepository.findById(projectId).ifPresent {
                projectRepository.deleteByProjectId(projectId)
            }
        }
    }


}