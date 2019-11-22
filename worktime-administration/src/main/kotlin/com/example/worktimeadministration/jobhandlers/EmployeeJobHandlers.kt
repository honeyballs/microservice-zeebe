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

/**
 * This class contains the functions used by the Zeebe JobHandlers to execute logic when a job is received.
 * The functions are stored in variables, so they can be passed to the handlers.
 *
 * A singleton instance of this class is created when the application context is initialized.
 * This way a repository and the ObjectMapper (used for JSON Deserialization) can be injected into the class to use them in these functions.
 *
 */
@Component
class EmployeeJobHandlers(
        val employeeRepository: WorktimeEmployeeRepository,
        val objectMapper: ObjectMapper
) {


    val synchronizeEmployees: (JobClient, ActivatedJob) -> Unit = { jobClient, job ->
        println("EXECUTING SYNC JOB")
        val employeeDto = objectMapper.readValue<EmployeeSyncDto>(job.variablesAsMap["employee"] as String)
        try {
            // If an employee is namend FAIL, we throw an exception to test the rollback functionality
            if (employeeDto.lastname == "FAIL") {
                throw Exception("TEST ROLLBACK EXCEPTION")
            }
            employeeRepository.findById(employeeDto.id).ifPresentOrElse({
                it.firstname = employeeDto.firstname
                it.lastname = employeeDto.lastname
                it.deleted = employeeDto.deleted
                it.state = employeeDto.state
                employeeRepository.save(it)
            }) {
                val worktimeEmployee = WorktimeEmployee(null, employeeDto.id, employeeDto.firstname, employeeDto.lastname, employeeDto.deleted, employeeDto.state)
                employeeRepository.save(worktimeEmployee)
            }
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

    val compensateEmployee: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val employeeId = objectMapper.readValue<EmployeeSyncDto>(job.variablesAsMap["employee"] as String).id
        if (variables.containsKey("compensationEmployee")) {
            val compensationEmployee = objectMapper.readValue<EmployeeSyncDto>(variables["compensationEmployee"] as String)
            employeeRepository.findByEmployeeId(employeeId).ifPresent {
                it.firstname = compensationEmployee.firstname
                it.lastname = compensationEmployee.lastname
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