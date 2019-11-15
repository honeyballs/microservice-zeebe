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
        val employeeDto = objectMapper.readValue<EmployeeSyncDto>(job.variablesAsMap["employee"] as String)
        try {
            // Retrieve and update the employee or create it if it doesn't exist
            employeeRepository.findByEmployeeId(employeeDto.id).ifPresentOrElse({
                it.firstname = employeeDto.firstname
                it.lastname = employeeDto.lastname
                it.mail = employeeDto.mail
                it.department = employeeDto.department
                it.title = employeeDto.department
                it.deleted = employeeDto.deleted
                it.state = employeeDto.state
                employeeRepository.save(it)
            }) {
                val projectEmployee = ProjectEmployee(null, employeeDto.id, employeeDto.firstname, employeeDto.lastname, employeeDto.mail, employeeDto.department, employeeDto.title, employeeDto.deleted, employeeDto.state)
                employeeRepository.save(projectEmployee)
            }
            val result = mapOf("projectEmployeeSynced" to true)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        } catch (e: Exception) {
            val result = mapOf("projectEmployeeSynced" to false)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        }
    }

    val activateEmployee: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING ACTIVATION JOB")
        val employeeId = objectMapper.readValue<EmployeeSyncDto>(job.variablesAsMap["employee"] as String).id
        val employee = employeeRepository.findByEmployeeId(employeeId).orElseThrow()
        employee.state = AggregateState.ACTIVE
        employeeRepository.save(employee)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensateEmployee: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val employeeId = objectMapper.readValue<EmployeeSyncDto>(variables["employee"] as String).id
        if (variables.containsKey("compensationEmployee")) {
            val compensationEmployee = objectMapper.readValue<EmployeeSyncDto>(variables["compensationEmployee"] as String)
            employeeRepository.findByEmployeeId(employeeId).ifPresent {
                it.firstname = compensationEmployee.firstname
                it.lastname = compensationEmployee.lastname
                it.mail = compensationEmployee.mail
                it.department = compensationEmployee.department
                it.title = compensationEmployee.title
                it.deleted = compensationEmployee.deleted
                it.state = AggregateState.ACTIVE
                employeeRepository.save(it)
            }
        } else {
            employeeRepository.findByEmployeeId(employeeId).ifPresent {
                employeeRepository.deleteByEmployeeId(employeeId)
            }
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

}