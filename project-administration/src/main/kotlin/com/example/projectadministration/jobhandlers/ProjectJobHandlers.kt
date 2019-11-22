package com.example.projectadministration.jobhandlers

import com.example.projectadministration.model.AggregateState
import com.example.projectadministration.model.dto.ProjectSyncDto
import com.example.projectadministration.model.employee.ProjectEmployee
import com.example.projectadministration.model.employee.dto.EmployeeSyncDto
import com.example.projectadministration.repositories.ProjectEmployeeRepository
import com.example.projectadministration.repositories.ProjectRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import org.springframework.stereotype.Component
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
        val projectRepository: ProjectRepository,
        val employeeRepository: ProjectEmployeeRepository,
        val mapper: ObjectMapper
) {

    val activateProject: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING ACTIVATION JOB")
        val projectId = mapper.readValue<ProjectSyncDto>(job.variablesAsMap["project"] as String).id
        val project = projectRepository.findById(projectId).orElseThrow()
        project.state = AggregateState.ACTIVE
        projectRepository.save(project)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensateProject: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val projectId = mapper.readValue<ProjectSyncDto>(job.variablesAsMap["project"] as String).id
        if (variables.containsKey("compensationProject")) {
            handleCompensationTransactional(projectId, mapper.readValue<ProjectSyncDto>(job.variablesAsMap["compensationProject"] as String))
        } else {
            handleCompensationTransactional(projectId, null)
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    fun handleCompensationTransactional(projectId: Long, compensationProject: ProjectSyncDto?) {
        if (compensationProject != null) {
            projectRepository.findById(projectId).ifPresent { project ->
                project.endDate = compensationProject.endDate
                // Add employees to project if necessary
                compensationProject.employees.filter {id ->  !project.employees.map { it.employeeId }.contains(id) }.forEach {
                    project.employees.add(employeeRepository.findByEmployeeId(it).orElseThrow())
                }
                // Remove employees from project if necessary
                project.employees.map { it.id }.filter {id ->  !compensationProject.employees.map { it }.contains(id) }.forEach {
                    project.employees.removeIf {t: ProjectEmployee -> t.employeeId == it }
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