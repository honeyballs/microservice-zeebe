package com.example.projectadministration.configuration

import com.example.projectadministration.jobhandlers.DepartmentJobHandlers
import com.example.projectadministration.jobhandlers.EmployeeJobHandlers
import com.example.projectadministration.jobhandlers.PositionJobHandlers
import com.example.projectadministration.jobhandlers.ProjectJobHandlers
import io.zeebe.client.ZeebeClient
import io.zeebe.client.api.worker.JobHandler
import io.zeebe.client.api.worker.JobWorker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.Transactional

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
    lateinit var departmentHandlers: DepartmentJobHandlers

    @Autowired
    lateinit var positionHandlers: PositionJobHandlers

    @Autowired
    lateinit var handlers: EmployeeJobHandlers

    @Autowired
    lateinit var projectHandlers: ProjectJobHandlers

    @Bean
    fun setDepartmentSyncWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("project-sync-department")
                .handler(JobHandler(departmentHandlers.synchronizeDepartments))
                .fetchVariables("department")
                .open()
    }

    @Bean
    fun setDepartmentActiveWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("activate-project-department")
                .handler(JobHandler(departmentHandlers.activateDepartment))
                .fetchVariables("department")
                .open()
    }

    @Bean
    fun setDepartmentCompensationWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("compensate-project-department")
                .handler(JobHandler(departmentHandlers.compensateDepartment))
                .fetchVariables("department", "compensationDepartment")
                .open()
    }

    @Bean
    fun setPositionSyncWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("project-sync-position")
                .handler(JobHandler(positionHandlers.synchronizePosition))
                .fetchVariables("position")
                .open()
    }

    @Bean
    fun setPositionActiveWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("activate-project-position")
                .handler(JobHandler(positionHandlers.activatePosition))
                .fetchVariables("position")
                .open()
    }

    @Bean
    fun setPositionCompensationWorker(): JobWorker {
        println("Synchronisation Worker created")
        return client.newWorker()
                .jobType("compensate-project-position")
                .handler(JobHandler(positionHandlers.compensatePosition))
                .fetchVariables("department", "compensationDepartment")
                .open()
    }

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