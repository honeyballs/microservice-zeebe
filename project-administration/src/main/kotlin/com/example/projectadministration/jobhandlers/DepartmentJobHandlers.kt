package com.example.projectadministration.jobhandlers

import com.example.projectadministration.model.aggregates.AggregateState
import com.example.projectadministration.model.employee.Department
import com.example.projectadministration.model.employee.DepartmentSync
import com.example.projectadministration.model.employee.Employee
import com.example.projectadministration.model.employee.EmployeeSync
import com.example.projectadministration.repositories.employee.DepartmentRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.UnexpectedRollbackException
import org.springframework.transaction.annotation.Transactional

@Component
class DepartmentJobHandlers(
        val departmentSyncTransactionService: DepartmentSyncTransactionService,
        val objectMapper: ObjectMapper
) {


    val synchronizeDepartments: (JobClient, ActivatedJob) -> Unit = { jobClient, job ->
        println("EXECUTING DEPARTMENT SYNC JOB")
        val departmentDto = objectMapper.readValue<DepartmentSync>(job.variablesAsMap["department"] as String)
        try {
            departmentSyncTransactionService.handleSynchronization(departmentDto)
            // If no exception was thrown the synchronization was successful.
            // A String Boolean map is used to define new process variables returned to Zeebe by this job.
            val result = mapOf("projectDepartmentSynced" to true)
            // Using the provided JobClient we tell Zeebe that the job is complete and pass our map as new process variables
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        } catch (e: Exception) {
            e.printStackTrace()
            // If an exception was thrown the job is also complete, but set the map value to false.
            val result = mapOf("projectDepartmentSynced" to false)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        }
    }

    val activateDepartment: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING DEPARTMENT ACTIVATION JOB")
        val departmentId = objectMapper.readValue<DepartmentSync>(job.variablesAsMap["department"] as String).id
        departmentSyncTransactionService.handleActivation(departmentId)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensateDepartment: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING DEPARTMENT COMPENSATION JOB")
        val variables = job.variablesAsMap
        val departmentId = objectMapper.readValue<DepartmentSync>(variables["department"] as String).id
        if (variables.containsKey("compensationDepartment")) {
            departmentSyncTransactionService.handleCompensation(departmentId, objectMapper.readValue<DepartmentSync>(variables["compensationDepartment"] as String))
        } else {
            departmentSyncTransactionService.handleCompensation(departmentId, null)
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }


}

@Service
class DepartmentSyncTransactionService(
        val departmentRepository: DepartmentRepository
): SyncService<DepartmentSync> {

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleSynchronization(departmentDto: DepartmentSync) {
        // Retrieve and update the department or create it if it doesn't exist
        departmentRepository.findByDepartmentId(departmentDto.id).ifPresentOrElse({
            it.name = departmentDto.name
            it.deleted = departmentDto.deleted
            it.state = departmentDto.state
            departmentRepository.save(it)
        }) {
            val projectDepartment = Department(
                    null,
                    departmentDto.id,
                    departmentDto.name,
                    departmentDto.deleted,
                    departmentDto.state
            )
            departmentRepository.save(projectDepartment)
        }
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleActivation(departmentId: Long) {
        val department = departmentRepository.findByDepartmentId(departmentId).orElseThrow()
        department.state = AggregateState.ACTIVE
        departmentRepository.save(department)
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleCompensation(departmentId: Long, compensationDepartment: DepartmentSync?) {
        if (compensationDepartment != null) {
            departmentRepository.findByDepartmentId(departmentId).ifPresent {
                it.name = compensationDepartment.name
                it.deleted = compensationDepartment.deleted
                it.state = AggregateState.ACTIVE
                departmentRepository.save(it)
            }
        } else {
            departmentRepository.findByDepartmentId(departmentId).ifPresent {
                departmentRepository.deleteByDepartmentId(departmentId)
            }
        }
    }

}