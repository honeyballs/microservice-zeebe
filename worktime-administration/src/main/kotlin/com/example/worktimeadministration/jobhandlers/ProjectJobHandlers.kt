package com.example.worktimeadministration.jobhandlers

import com.example.worktimeadministration.model.AggregateState
import com.example.worktimeadministration.model.employee.WorktimeEmployee
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

@Component
class ProjectJobHandlers(
        val projectRepository: WorktimeProjectRepository,
        val mapper: ObjectMapper,
        val employeeRepository: WorktimeEmployeeRepository
) {

    val synchronizeProjects: (JobClient, ActivatedJob) -> Unit = { jobClient, job ->
        println("EXECUTING SYNC JOB")
        val projectDto = mapper.readValue<ProjectSyncDto>(job.variablesAsMap["project"] as String)
        try {
            handleSynchronizationTransactional(projectDto)
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
    fun handleSynchronizationTransactional(projectDto: ProjectSyncDto) {
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

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    fun handleCompensationTransactional(projectId: Long, compensationProject: ProjectSyncDto?) {
        if (compensationProject != null) {
            projectRepository.findByProjectId(projectId).ifPresent { project ->
                project.endDate = compensationProject.endDate
                // Add employees to project if necessary
                compensationProject.employees.filter {id ->  !project.employees.map { it.employeeId }.contains(id) }.forEach {
                    project.employees.add(employeeRepository.findByEmployeeId(it).orElseThrow())
                }
                // Remove employees from project if necessary
                project.employees.map { it.id }.filter {id ->  !compensationProject.employees.map { it }.contains(id) }.forEach {
                    project.employees.removeIf {t: WorktimeEmployee -> t.employeeId == it }
                }
                project.deleted = compensationProject.deleted
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