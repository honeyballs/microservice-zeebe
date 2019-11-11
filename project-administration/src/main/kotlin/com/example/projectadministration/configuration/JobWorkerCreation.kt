package com.example.projectadministration.configuration

import com.example.projectadministration.jobhandlers.EmployeeJobHandlers
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
    fun setEmployeeCreationWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("project-sync-employee")
                .handler(JobHandler(handlers.synchronizeEmployees))
                .fetchVariables("employee")
                .open()
    }

    @Bean
    fun setEmployeeActiveWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("activate-project-employee")
                .handler(JobHandler(handlers.activateEmployee))
                .fetchVariables("employee")
                .open()
    }

    @Bean
    fun setEmployeeFailedWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("fail-project-employee")
                .handler(JobHandler(handlers.failEmployee))
                .fetchVariables("employee")
                .open()
    }

}