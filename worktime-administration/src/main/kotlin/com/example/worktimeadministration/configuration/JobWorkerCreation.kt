package com.example.worktimeadministration.configuration

import com.example.worktimeadministration.jobhandlers.EmployeeJobHandlers
import com.example.worktimeadministration.jobhandlers.ProjectJobHandlers
import io.zeebe.client.ZeebeClient
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

    @Autowired
    lateinit var projectHandlers: ProjectJobHandlers

    @Bean
    fun setEmployeeSyncWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("worktime-sync-employee")
                .handler(JobHandler(handlers.synchronizeEmployees))
                .fetchVariables("employee")
                .open()
    }

    @Bean
    fun setEmployeeActiveWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("activate-worktime-employee")
                .handler(JobHandler(handlers.activateEmployee))
                .fetchVariables("employee")
                .open()
    }

    @Bean
    fun setEmployeeCompensationWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("compensate-worktime-employee")
                .handler(JobHandler(handlers.compensateEmployee))
                .fetchVariables("employee", "compensationEmployee")
                .open()
    }

    @Bean
    fun setProjectSyncWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("worktime-sync-project")
                .handler(JobHandler(projectHandlers.synchronizeProjects))
                .fetchVariables("project")
                .open()
    }

    @Bean
    fun setProjectActiveWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("activate-worktime-project")
                .handler(JobHandler(projectHandlers.activateProject))
                .fetchVariables("project")
                .open()
    }

    @Bean
    fun setProjectCompensationWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("compensate-worktime-project")
                .handler(JobHandler(projectHandlers.compensateProject))
                .fetchVariables("project", "compensationProject")
                .open()
    }

}