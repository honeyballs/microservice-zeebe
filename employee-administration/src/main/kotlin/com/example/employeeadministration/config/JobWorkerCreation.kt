package com.example.employeeadministration.config

import com.example.employeeadministration.jobhandlers.EmployeeJobHandlers
import io.zeebe.client.ZeebeClient
import io.zeebe.client.api.worker.JobHandler
import io.zeebe.client.api.worker.JobWorker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Defines all JobWorker's of this Service using the injected ZeebeClient.
 * All JobWorkers react to a type if job and contain a JobHandler which invokes a passed handler function.
 * Lastly the process variables the job needs are specified.
 */
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
                .jobType("compensate-employee")
                .handler(JobHandler(handlers.compensateEmployee))
                .fetchVariables("employee", "compensationEmployee")
                .open()
    }

}