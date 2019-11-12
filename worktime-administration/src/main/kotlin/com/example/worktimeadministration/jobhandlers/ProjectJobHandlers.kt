package com.example.worktimeadministration.jobhandlers

import com.example.worktimeadministration.model.AggregateState
import com.example.worktimeadministration.model.employee.WorktimeEmployee
import com.example.worktimeadministration.model.employee.dto.EmployeeSyncDto
import com.example.worktimeadministration.model.project.WorktimeProject
import com.example.worktimeadministration.model.project.dto.ProjectSyncDto
import com.example.worktimeadministration.repositories.WorktimeEmployeeRepository
import com.example.worktimeadministration.repositories.WorktimeProjectRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import org.springframework.stereotype.Component
import org.springframework.transaction.UnexpectedRollbackException
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Component
class ProjectJobHandlers(
        val projectRepository: WorktimeProjectRepository,
        val mapper: ObjectMapper,
        val employeeRepository: WorktimeEmployeeRepository
) {

    val synchronizeProjects: (JobClient, ActivatedJob) -> Unit = { jobClient, job ->
        println("EXECUTING SYNC JOB")
        val projectDto = mapProjectVariableToDto(job.variablesAsMap)
        try {
            handleSynchronization(projectDto)
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
        val projectId = mapProjectVariableToDto(job.variablesAsMap).id
        val project = projectRepository.findById(projectId).orElseThrow()
        project.state = AggregateState.ACTIVE
        projectRepository.save(project)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val failProject: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING FAILURE JOB")
        val projectId = mapProjectVariableToDto(job.variablesAsMap).id
        projectRepository.findById(projectId).ifPresent {
            it.state = AggregateState.FAILED
            projectRepository.save(it)
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    fun handleSynchronization(projectDto: ProjectSyncDto) {
        // Retrieve and update the employee or create it if it doesn't exist
        projectRepository.findById(projectDto.id).map { project ->
            project.endDate = projectDto.endDate
            project.deleted = projectDto.deleted
            project.state = projectDto.state
            // Add employees to project if necessary
            projectDto.employees.filter {id ->  !project.employees.map { it.employeeId }.contains(id) }.forEach {
                project.employees.add(employeeRepository.findByEmployeeIdAndDeletedFalse(it).orElseThrow())
            }
            // Remove employees from project if necessary
            project.employees.map { it.id }.filter {id ->  !projectDto.employees.map { it }.contains(id) }.forEach {
                project.employees.removeIf {t: WorktimeEmployee -> t.employeeId == it }
            }
            projectRepository.save(project)
        }.orElseGet {
            val employees = employeeRepository.findAllByEmployeeIdIn(projectDto.employees).toMutableSet()
            val worktimeProject = WorktimeProject(null, projectDto.id, projectDto.name, projectDto.startDate, projectDto.endDate, projectDto.deleted, projectDto.state, employees.toMutableSet())
            projectRepository.save(worktimeProject)
        }
    }


    fun mapProjectVariableToDto(variables: Map<String, Any>): ProjectSyncDto {
        val employeeJson = variables["project"] as String
        return mapper.readValue<ProjectSyncDto>(employeeJson)
    }


}