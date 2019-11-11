package com.example.employeeadministration.jobhandlers

import com.example.employeeadministration.model.AggregateState
import com.example.employeeadministration.model.Employee
import com.example.employeeadministration.model.dto.EmployeeSyncDto
import com.example.employeeadministration.repositories.EmployeeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component



@Component
class EmployeeJobHandlers {

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    val activateEmployee: (JobClient, ActivatedJob) -> Unit = {jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING ACTIVATION JOB")
        val employeeId = mapper.readValue<EmployeeSyncDto>(job.variablesAsMap["employee"] as String).id
        val employee = employeeRepository.findById(employeeId).orElseThrow()
        employee.state = AggregateState.ACTIVE
        val result = mapOf("employee" to mapper.writeValueAsString(employeeRepository.save(employee)))
        jobClient.newCompleteCommand(job.key)
                .variables(result)
                .send()
                .join()
    }

    val failEmployee: (JobClient, ActivatedJob) -> Unit = {jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING FAILURE JOB")
        val employeeId = mapper.readValue<EmployeeSyncDto>(job.variablesAsMap["employee"] as String).id
        val employee = employeeRepository.findById(employeeId).orElseThrow()
        employee.state = AggregateState.FAILED
        val result = mapOf("employee" to mapper.writeValueAsString(employeeRepository.save(employee)))
        jobClient.newCompleteCommand(job.key)
                .variables(result)
                .send()
                .join()
    }


}



