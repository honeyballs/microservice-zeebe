package com.example.projectadministration.jobhandlers

import com.example.projectadministration.model.AggregateState
import com.example.projectadministration.model.dto.ProjectSyncDto
import com.example.projectadministration.model.employee.dto.EmployeeSyncDto
import com.example.projectadministration.repositories.ProjectRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import org.springframework.stereotype.Component

@Component
class ProjectJobHandlers(val projectRepository: ProjectRepository, val mapper: ObjectMapper) {

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
        val project = projectRepository.findById(projectId).orElseThrow()
        project.state = AggregateState.FAILED
        projectRepository.save(project)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    fun mapProjectVariableToDto(variables: Map<String, Any>): ProjectSyncDto {
        val employeeJson = variables["project"] as String
        return mapper.readValue<ProjectSyncDto>(employeeJson)
    }


}