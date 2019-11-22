package com.example.projectadministration.configuration

import com.example.projectadministration.jobhandlers.EmployeeJobHandlers
import com.example.projectadministration.jobhandlers.ProjectJobHandlers
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

    @Autowired
    lateinit var projectHandlers: ProjectJobHandlers

    @Bean
    fun setEmployeeSyncWorker(): JobWorker {
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
    fun setEmployeeCompensationWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("compensate-project-employee")
                .handler(JobHandler(handlers.compensateEmployee))
                .fetchVariables("employee", "compensationEmployee")
                .open()
    }

    @Bean
    fun setProjectActiveWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("activate-project")
                .handler(JobHandler(projectHandlers.activateProject))
                .fetchVariables("project")
                .open()
    }

    @Bean
    fun setProjectCompensationWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("compensate-project")
                .handler(JobHandler(projectHandlers.compensateProject))
                .fetchVariables("project", "compensationProject")
                .open()
    }

}