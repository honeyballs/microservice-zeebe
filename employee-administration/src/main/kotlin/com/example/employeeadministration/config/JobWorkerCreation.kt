package com.example.employeeadministration.config

import com.example.employeeadministration.jobhandlers.EmployeeJobHandlers
import com.example.employeeadministration.model.AggregateState
import com.example.employeeadministration.model.Employee
import com.example.employeeadministration.repositories.EmployeeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.ZeebeClient
import io.zeebe.client.api.worker.JobClient
import io.zeebe.client.api.worker.JobHandler
import io.zeebe.client.api.worker.JobWorker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobWorkerCreation {

    @Autowired
    lateinit var client: ZeebeClient

    @Autowired
    lateinit var handlers: EmployeeJobHandlers

    @Bean
    fun setEmployeeActiveWorker(): JobWorker {
        println("Activation Worker created")
        return client.newWorker()
                .jobType("activate-employee")
                .handler(JobHandler(handlers.activateEmployee))
                .fetchVariables("employee")
                .open()
    }

    @Bean
    fun setEmployeeFailedWorker(): JobWorker {
        println("Failure Worker created")
        return client.newWorker()
                .jobType("fail-employee")
                .handler(JobHandler(handlers.failEmployee))
                .fetchVariables("employee")
                .open()
    }

}