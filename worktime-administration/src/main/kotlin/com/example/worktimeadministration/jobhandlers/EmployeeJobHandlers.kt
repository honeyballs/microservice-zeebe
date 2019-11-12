package com.example.worktimeadministration.jobhandlers


import com.example.worktimeadministration.model.AggregateState
import com.example.worktimeadministration.model.employee.WorktimeEmployee
import com.example.worktimeadministration.model.employee.dto.EmployeeSyncDto
import com.example.worktimeadministration.model.project.WorktimeProject
import com.example.worktimeadministration.repositories.WorktimeEmployeeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import org.springframework.stereotype.Component
import org.springframework.transaction.UnexpectedRollbackException
import org.springframework.transaction.annotation.Transactional

@Component
class EmployeeJobHandlers(
        val employeeRepository: WorktimeEmployeeRepository,
        val objectMapper: ObjectMapper
) {


    val synchronizeEmployees: (JobClient, ActivatedJob) -> Unit = { jobClient, job ->
        println("EXECUTING SYNC JOB")
        val employeeDto = mapEmployeeVariableToDto(job.variablesAsMap)
        try {
            handleSynchronisation(employeeDto)
            val result = mapOf("worktimeEmployeeSynced" to true)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        } catch (e: Exception) {
            e.printStackTrace()
            val result = mapOf("worktimeEmployeeSynced" to false)
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
        employeeRepository.save(employee)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val failEmployee: (JobClient, ActivatedJob) -> Unit = {jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING FAILURE JOB")
        val employeeId = objectMapper.readValue<EmployeeSyncDto>(job.variablesAsMap["employee"] as String).id
        employeeRepository.findById(employeeId).ifPresent {
            it.state = AggregateState.FAILED
            employeeRepository.save(it)
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    fun handleSynchronisation(employeeDto: EmployeeSyncDto) {
        // Retrieve and update the employee or create it if it doesn't exist
        employeeRepository.findById(employeeDto.id).map {
            it.firstname = employeeDto.firstname
            it.lastname = employeeDto.lastname
            it.deleted = employeeDto.deleted
            it.state = employeeDto.state
            employeeRepository.save(it)
        }.orElseGet {
            val worktimeEmployee = WorktimeEmployee(null, employeeDto.id, employeeDto.firstname, employeeDto.lastname, employeeDto.deleted, employeeDto.state)
            employeeRepository.save(worktimeEmployee)
        }
    }

    fun mapEmployeeVariableToDto(variables: Map<String, Any>): EmployeeSyncDto {
        val employeeJson = variables["employee"] as String
        return objectMapper.readValue<EmployeeSyncDto>(employeeJson)
    }


}