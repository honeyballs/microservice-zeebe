package com.example.projectadministration.jobhandlers

import com.example.projectadministration.model.AggregateState
import com.example.projectadministration.model.employee.ProjectEmployee
import com.example.projectadministration.model.employee.dto.EmployeeSyncDto
import com.example.projectadministration.repositories.ProjectEmployeeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import org.springframework.boot.autoconfigure.batch.BatchProperties
import org.springframework.stereotype.Component
import org.springframework.transaction.UnexpectedRollbackException
import java.util.function.Supplier

@Component
class EmployeeJobHandlers(
        val employeeRepository: ProjectEmployeeRepository,
        val objectMapper: ObjectMapper
) {


    val synchronizeEmployees: (JobClient, ActivatedJob) -> Unit = { jobClient, job ->
        println("EXECUTING SYNC JOB")
        val employeeDto = mapEmployeeVariableToDto(job.variablesAsMap)
        try {
            // Retrieve and update the employee or create it if it doesn't exist
            val employee = employeeRepository.findByEmployeeId(employeeDto.id).map {
                it.firstname = employeeDto.firstname
                it.lastname = employeeDto.lastname
                it.mail = employeeDto.mail
                it.department = employeeDto.department
                it.title = employeeDto.department
                it.deleted = employeeDto.deleted
                it.state = employeeDto.state
            }.orElseGet {
                val projectEmployee = ProjectEmployee(null, employeeDto.id, employeeDto.firstname, employeeDto.lastname, employeeDto.mail, employeeDto.department, employeeDto.title, employeeDto.deleted, employeeDto.state)
                employeeRepository.save(projectEmployee)
            }
            val result = mapOf("projectSynced" to true)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        } catch (e: Exception) {
            val result = mapOf("projectSynced" to false)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        }
    }

    val activateEmployee: (JobClient, ActivatedJob) -> Unit = {jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING ACTIVATION JOB")
        val employeeId = objectMapper.readValue<EmployeeSyncDto>(job.variablesAsMap["employee"] as String).id
        val employee = employeeRepository.findById(employeeId).orElseThrow()
        employee.state = AggregateState.ACTIVE
        val result = mapOf("employee" to objectMapper.writeValueAsString(employeeRepository.save(employee)))
        jobClient.newCompleteCommand(job.key)
                .variables(result)
                .send()
                .join()
    }

    val failEmployee: (JobClient, ActivatedJob) -> Unit = {jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING FAILURE JOB")
        val employeeId = objectMapper.readValue<EmployeeSyncDto>(job.variablesAsMap["employee"] as String).id
        val employee = employeeRepository.findById(employeeId).orElseThrow()
        employee.state = AggregateState.FAILED
        val result = mapOf("employee" to objectMapper.writeValueAsString(employeeRepository.save(employee)))
        jobClient.newCompleteCommand(job.key)
                .variables(result)
                .send()
                .join()
    }

    fun mapEmployeeVariableToDto(variables: Map<String, Any>): EmployeeSyncDto {
        val employeeJson = variables["employee"] as String
        return objectMapper.readValue<EmployeeSyncDto>(employeeJson)
    }


}