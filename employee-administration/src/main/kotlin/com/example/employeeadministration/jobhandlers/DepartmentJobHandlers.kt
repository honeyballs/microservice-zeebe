package com.example.employeeadministration.jobhandlers

import com.example.employeeadministration.model.aggregates.AggregateState
import com.example.employeeadministration.model.dto.DepartmentSync
import com.example.employeeadministration.model.dto.EmployeeSync
import com.example.employeeadministration.repositories.DepartmentRepository
import com.example.employeeadministration.repositories.EmployeeRepository
import com.example.employeeadministration.repositories.PositionRepository
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
class DepartmentJobHandlers {

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var departmentSyncTransactionService: DepartmentSyncTransactionService

    val activateDepartment: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING ACTIVATION JOB")
        val departmentId = mapper.readValue<DepartmentSync>(job.variablesAsMap["department"] as String).id
        departmentSyncTransactionService.handleActivation(departmentId)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensateDepartment: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val departmentId = mapper.readValue<DepartmentSync>(job.variablesAsMap["department"] as String).id
        if (variables.containsKey("compensationDepartment")) {
            departmentSyncTransactionService.handleCompensation(departmentId, mapper.readValue<DepartmentSync>(variables["compensationDepartment"] as String))
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
    override fun handleActivation(departmentId: Long) {
        val department = departmentRepository.findById(departmentId).orElseThrow()
        department.state = AggregateState.ACTIVE
        departmentRepository.save(department)
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleCompensation(departmentId: Long, compensationDepartment: DepartmentSync?) {
        if (compensationDepartment != null) {
            departmentRepository.findById(departmentId).ifPresent {
                it.name = compensationDepartment.name
                it.deleted = compensationDepartment.deleted
                it.state = AggregateState.ACTIVE
                departmentRepository.save(it)
            }
        } else {
            departmentRepository.findById(departmentId).ifPresent {
                departmentRepository.deleteById(departmentId)
            }
        }
    }

}