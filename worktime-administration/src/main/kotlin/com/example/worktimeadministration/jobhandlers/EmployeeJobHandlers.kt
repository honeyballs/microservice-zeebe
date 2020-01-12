package com.example.worktimeadministration.jobhandlers


import com.example.worktimeadministration.model.aggregates.AggregateState
import com.example.worktimeadministration.model.employee.Employee
import com.example.worktimeadministration.model.employee.EmployeeSync
import com.example.worktimeadministration.repositories.employee.EmployeeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.UnexpectedRollbackException
import org.springframework.transaction.annotation.Transactional

/**
 * This class contains the functions used by the Zeebe JobHandlers to execute logic when a job is received.
 * The functions are stored in variables, so they can be passed to the handlers.
 *
 * A singleton instance of this class is created when the application context is initialized.
 * This way a repository and the ObjectMapper (used for JSON Deserialization) can be injected into the class to use them in these functions.
 *
 */
@Component
class EmployeeJobHandlers(
        val employeeSyncTransactionService: EmployeeSyncTransactionService,
        val objectMapper: ObjectMapper
) {


    val synchronizeEmployees: (JobClient, ActivatedJob) -> Unit = { jobClient, job ->
        println("EXECUTING SYNC JOB")
        val employeeDto = objectMapper.readValue<EmployeeSync>(job.variablesAsMap["employee"] as String)
        try {
            employeeSyncTransactionService.handleSynchronization(employeeDto)
            val result = mapOf("worktimeEmployeeSynced" to true)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        } catch (e: Exception) {
            e.printStackTrace()
            val result = mapOf("worktimeEmployeeSynced" to false)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        }
    }

    val activateEmployee: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING ACTIVATION JOB")
        val employeeId = objectMapper.readValue<EmployeeSync>(job.variablesAsMap["employee"] as String).id
        employeeSyncTransactionService.handleActivation(employeeId)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensateEmployee: (JobClient, ActivatedJob) -> Unit =  { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val employeeId = objectMapper.readValue<EmployeeSync>(job.variablesAsMap["employee"] as String).id
        if (variables.containsKey("compensationEmployee")) {
            employeeSyncTransactionService.handleCompensation(employeeId, objectMapper.readValue<EmployeeSync>(variables["compensationEmployee"] as String))
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
        val employeeRepository: EmployeeRepository
): SyncService<EmployeeSync> {

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleSynchronization(employeeDto: EmployeeSync) {
        employeeRepository.findByEmployeeId(employeeDto.id).ifPresentOrElse({
            it.firstname = employeeDto.firstname
            it.lastname = employeeDto.lastname
            it.companyMail = employeeDto.companyMail
            it.deleted = employeeDto.deleted
            it.state = employeeDto.state
            employeeRepository.save(it)
        }) {
            val worktimeEmployee = Employee(
                    null,
                    employeeDto.id,
                    employeeDto.firstname,
                    employeeDto.lastname,
                    employeeDto.companyMail,
                    employeeDto.availableVacationHours,
                    0,
                    employeeDto.deleted,
                    employeeDto.state
            )
            employeeRepository.save(worktimeEmployee)
        }
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleActivation(id: Long) {
        val employee = employeeRepository.findByEmployeeId(id).orElseThrow()
        employee.state = AggregateState.ACTIVE
        employeeRepository.save(employee)
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleCompensation(id: Long, compensationEmployee: EmployeeSync?) {
        if (compensationEmployee != null) {
            employeeRepository.findByEmployeeId(id).ifPresent {
                it.firstname = compensationEmployee.firstname
                it.lastname = compensationEmployee.lastname
                it.companyMail = compensationEmployee.companyMail
                it.deleted = compensationEmployee.deleted
                it.state = AggregateState.ACTIVE
                employeeRepository.save(it)
            }
        } else {
            employeeRepository.findByEmployeeId(id).ifPresent {
                employeeRepository.deleteByEmployeeId(id)
            }
        }
    }

}