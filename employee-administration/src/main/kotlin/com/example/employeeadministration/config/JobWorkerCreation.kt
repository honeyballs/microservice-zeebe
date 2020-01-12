package com.example.employeeadministration.config

import com.example.employeeadministration.jobhandlers.DepartmentJobHandlers
import com.example.employeeadministration.jobhandlers.EmployeeJobHandlers
import com.example.employeeadministration.jobhandlers.PositionJobHandlers
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
    lateinit var departmentHandlers: DepartmentJobHandlers

    @Autowired
    lateinit var positionJobHandlers: PositionJobHandlers

    @Bean
    fun setEmployeeActiveWorker(): JobWorker {
        println("Employee Activation Worker created")
        return client.newWorker()
                .jobType("activate-employee")
                .handler(JobHandler(handlers.activateEmployee))
                .fetchVariables("employee")
                .open()
    }

    @Bean
    fun setEmployeeFailedWorker(): JobWorker {
        println("Employee Failure Worker created")
        return client.newWorker()
                .jobType("compensate-employee")
                .handler(JobHandler(handlers.compensateEmployee))
                .fetchVariables("employee", "compensationEmployee")
                .open()
    }

    @Bean
    fun setDepartmentActiveWorker(): JobWorker {
        println("Department Activation Worker created")
        return client.newWorker()
                .jobType("activate-department")
                .handler(JobHandler(departmentHandlers.activateDepartment))
                .fetchVariables("department")
                .open()
    }

    @Bean
    fun setDepartmentFailedWorker(): JobWorker {
        println("Department Failure Worker created")
        return client.newWorker()
                .jobType("compensate-department")
                .handler(JobHandler(departmentHandlers.compensateDepartment))
                .fetchVariables("department", "compensationDepartment")
                .open()
    }

    @Bean
    fun setPositionActiveWorker(): JobWorker {
        println("Position Activation Worker created")
        return client.newWorker()
                .jobType("activate-position")
                .handler(JobHandler(positionJobHandlers.activatePosition))
                .fetchVariables("position")
                .open()
    }

    @Bean
    fun setPositionFailedWorker(): JobWorker {
        println("Position Failure Worker created")
        return client.newWorker()
                .jobType("compensate-position")
                .handler(JobHandler(positionJobHandlers.compensatePosition))
                .fetchVariables("position", "compensationPosition")
                .open()
    }
}