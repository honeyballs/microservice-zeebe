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


/**
 * This class contains the functions used by the Zeebe JobHandlers to execute logic when a job is received.
 * The functions are stored in variables, so they can be passed to the handlers.
 *
 * A singleton instance of this class is created when the application context is initialized.
 * This way repositories and the ObjectMapper (used for JSON Deserialization) can be injected into the class to use them in these functions.
 *
 */
@Component
class EmployeeJobHandlers {

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var employeeSyncTransactionService: EmployeeSyncTransactionService

    val activateEmployee: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING ACTIVATION JOB")
        val employeeId = mapper.readValue<EmployeeSync>(job.variablesAsMap["employee"] as String).id
        employeeSyncTransactionService.handleActivation(employeeId)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensateEmployee: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val employeeId = mapper.readValue<EmployeeSync>(job.variablesAsMap["employee"] as String).id
        if (variables.containsKey("compensationEmployee")) {
            employeeSyncTransactionService.handleCompensation(employeeId, mapper.readValue<EmployeeSync>(variables["compensationEmployee"] as String))
        } else {
            employeeSyncTransactionService.handleCompensation(employeeId, null)
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

}

@Service
class EmployeeSyncTransactionService(
        val employeeRepository: EmployeeRepository,
        val departmentRepository: DepartmentRepository,
        val positionRepository: PositionRepository
): SyncService<EmployeeSync> {

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleActivation(employeeId: Long) {
        val employee = employeeRepository.findById(employeeId).orElseThrow()
        employee.state = AggregateState.ACTIVE
        employeeRepository.save(employee)
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleCompensation(employeeId: Long, compensationEmployee: EmployeeSync?) {
        if (compensationEmployee != null) {
            employeeRepository.findById(employeeId).ifPresent {
                it.firstname = compensationEmployee.firstname
                it.lastname = compensationEmployee.lastname
                it.address = compensationEmployee.address
                it.bankDetails = compensationEmployee.bankDetails
                it.companyMail = compensationEmployee.companyMail!!
                it.department = departmentRepository.findById(compensationEmployee.department).orElseThrow()
                it.position = positionRepository.findById(compensationEmployee.position).orElseThrow()
                it.hourlyRate = compensationEmployee.hourlyRate
                it.deleted = compensationEmployee.deleted
                it.state = AggregateState.ACTIVE
                employeeRepository.save(it)
            }
        } else {
            employeeRepository.findById(employeeId).ifPresent {
                employeeRepository.deleteById(employeeId)
            }
        }
    }
}



