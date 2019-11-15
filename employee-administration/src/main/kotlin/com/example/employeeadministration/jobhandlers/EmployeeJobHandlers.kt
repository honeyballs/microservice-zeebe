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
        employeeRepository.save(employee)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensateEmployee: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val employeeId = mapper.readValue<EmployeeSyncDto>(job.variablesAsMap["employee"] as String).id
        if (variables.containsKey("compensationEmployee")) {
            val compensationEmployee = mapper.readValue<EmployeeSyncDto>(variables["compensationEmployee"] as String)
            employeeRepository.findById(employeeId).ifPresent {
                it.firstname = compensationEmployee.firstname
                it.lastname = compensationEmployee.lastname
                it.address = compensationEmployee.address
                it.iban = compensationEmployee.iban
                it.mail = compensationEmployee.mail
                it.department = compensationEmployee.department
                it.title = compensationEmployee.title
                it.hourlyRate = compensationEmployee.hourlyRate
                it.deleted = compensationEmployee.deleted
                it.state = AggregateState.ACTIVE
                employeeRepository.save(it)
            }
        } else {
            employeeRepository.findById(employeeId).ifPresent {
                employeeRepository.deleteById(employeeId)
            }
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }


}



