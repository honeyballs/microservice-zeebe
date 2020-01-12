package com.example.projectadministration.jobhandlers

import com.example.projectadministration.model.aggregates.AggregateState
import com.example.projectadministration.model.dto.ProjectSync
import com.example.projectadministration.model.employee.Employee
import com.example.projectadministration.repositories.ProjectRepository
import com.example.projectadministration.repositories.employee.EmployeeRepository
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
 * This way repositories and the ObjectMapper (used for JSON Deserialization) can be injected into the class to use them in these functions.
 *
 */
@Component
class ProjectJobHandlers(
        val projectSyncService: ProjectSyncTransactionService,
        val mapper: ObjectMapper
) {

    val activateProject: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING ACTIVATION JOB")
        val projectId = mapper.readValue<ProjectSync>(job.variablesAsMap["project"] as String).id
        projectSyncService.handleActivation(projectId)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensateProject: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val projectId = mapper.readValue<ProjectSync>(job.variablesAsMap["project"] as String).id
        if (variables.containsKey("compensationProject")) {
            projectSyncService.handleCompensation(projectId, mapper.readValue<ProjectSync>(job.variablesAsMap["compensationProject"] as String))
        } else {
            projectSyncService.handleCompensation(projectId, null)
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

}

/**
 * To make the transactional annotations work the annotated methods have to be within another class
 */
@Service
class ProjectSyncTransactionService(
        val projectRepository: ProjectRepository,
        val employeeRepository: EmployeeRepository
): SyncService<ProjectSync> {

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleActivation(projectId: Long) {
        val project = projectRepository.findById(projectId).orElseThrow()
        project.state = AggregateState.ACTIVE
        projectRepository.save(project)
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleCompensation(projectId: Long, compensationProject: ProjectSync?) {
        if (compensationProject != null) {
            projectRepository.findById(projectId).ifPresent { project ->
                project.endDate = compensationProject.endDate
                project.description = compensationProject.description
                // Add employees to project if necessary
                compensationProject.employees.filter {id ->  !project.employees.map { it.employeeId }.contains(id) }.forEach {
                    project.employees.add(employeeRepository.findByEmployeeId(it).orElseThrow())
                }
                // Remove employees from project if necessary
                project.employees.map { it.employeeId }.filter {id ->  !compensationProject.employees.map { it }.contains(id) }.forEach {
                    project.employees.removeIf {t: Employee -> t.employeeId == it }
                }
                project.deleted = compensationProject.deleted
                project.state = AggregateState.ACTIVE
                projectRepository.save(project)
            }
        } else {
            projectRepository.findById(projectId).ifPresent {
                projectRepository.deleteById(projectId)
            }
        }
    }

}