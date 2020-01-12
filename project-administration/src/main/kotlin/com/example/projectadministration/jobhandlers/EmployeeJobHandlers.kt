package com.example.projectadministration.jobhandlers

import com.example.projectadministration.model.aggregates.AggregateState
import com.example.projectadministration.model.employee.Department
import com.example.projectadministration.model.employee.DepartmentSync
import com.example.projectadministration.model.employee.Employee
import com.example.projectadministration.model.employee.EmployeeSync
import com.example.projectadministration.repositories.employee.DepartmentRepository
import com.example.projectadministration.repositories.employee.EmployeeRepository
import com.example.projectadministration.repositories.employee.PositionRepository
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
            // If an employee is namend FAIL, we throw an exception to test the rollback functionality
            if (employeeDto.lastname == "FAIL") {
                throw Exception("TEST ROLLBACK EXCEPTION")
            }
            employeeSyncTransactionService.handleSynchronization(employeeDto)
            // If no exception was thrown the synchronization was successful.
            // A String Boolean map is used to define new process variables returned to Zeebe by this job.
            val result = mapOf("projectEmployeeSynced" to true)
            // Using the provided JobClient we tell Zeebe that the job is complete and pass our map as new process variables
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        } catch (e: Exception) {
            // If an exception was thrown the job is also complete, but set the map value to false.
            val result = mapOf("projectEmployeeSynced" to false)
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

    val compensateEmployee: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val employeeId = objectMapper.readValue<EmployeeSync>(variables["employee"] as String).id
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
        val employeeRepository: EmployeeRepository,
        val departmentRepository: DepartmentRepository,
        val positionRepository: PositionRepository
) : SyncService<EmployeeSync> {

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleSynchronization(employeeDto: EmployeeSync) {
        // Retrieve and update the employee or create it if it doesn't exist
        employeeRepository.findByEmployeeId(employeeDto.id).ifPresentOrElse({
            it.firstname = employeeDto.firstname
            it.lastname = employeeDto.lastname
            it.companyMail = employeeDto.companyMail
            it.department = departmentRepository.findByDepartmentId(employeeDto.department).orElseThrow()
            it.position = positionRepository.findByPositionId(employeeDto.position).orElseThrow()
            it.deleted = employeeDto.deleted
            it.state = employeeDto.state
            employeeRepository.save(it)
        }) {
            val projectEmployee = Employee(
                    null,
                    employeeDto.id,
                    employeeDto.firstname,
                    employeeDto.lastname,
                    departmentRepository.findByDepartmentId(employeeDto.department).orElseThrow(),
                    positionRepository.findByPositionId(employeeDto.position).orElseThrow(),
                    employeeDto.companyMail,
                    employeeDto.deleted,
                    employeeDto.state
            )
            employeeRepository.save(projectEmployee)
        }
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleActivation(employeeId: Long) {
        val employee = employeeRepository.findByEmployeeId(employeeId).orElseThrow()
        employee.state = AggregateState.ACTIVE
        employeeRepository.save(employee)
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleCompensation(employeeId: Long, compensationEmployee: EmployeeSync?) {
        if (compensationEmployee != null) {
            employeeRepository.findByEmployeeId(employeeId).ifPresent {
                it.firstname = compensationEmployee.firstname
                it.lastname = compensationEmployee.lastname
                it.companyMail = compensationEmployee.companyMail
                it.department = departmentRepository.findByDepartmentId(compensationEmployee.department).orElseThrow()
                it.position = positionRepository.findByPositionId(compensationEmployee.position).orElseThrow()
                it.deleted = compensationEmployee.deleted
                it.state = AggregateState.ACTIVE
                employeeRepository.save(it)
            }
        } else {
            employeeRepository.findByEmployeeId(employeeId).ifPresent {
                employeeRepository.deleteByEmployeeId(employeeId)
            }
        }
    }

}