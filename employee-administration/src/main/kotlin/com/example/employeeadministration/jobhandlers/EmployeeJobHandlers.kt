package com.example.employeeadministration.jobhandlers

import com.example.employeeadministration.model.AggregateState
import com.example.employeeadministration.model.Employee
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

    val handleEmployeeActivation: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING JOB")
        val employeeJson = job.variablesAsMap["employee"] as String
        var employee = mapper.readValue<Employee>(employeeJson)
        var dbEmployeeOptional = employeeRepository.findById(employee.id!!)
        val result = emptyMap<String, String>()
        if (dbEmployeeOptional.isPresent) {
            var dbEmployee = dbEmployeeOptional.get()
            dbEmployee.state = AggregateState.ACTIVE
            employee = employeeRepository.save(dbEmployee)
            result.plus(Pair("employee", mapper.writeValueAsString(employee)))
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        } else {
            jobClient.newFailCommand(job.key)
        }
    }

}



