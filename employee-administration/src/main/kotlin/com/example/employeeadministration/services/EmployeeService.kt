package com.example.employeeadministration.services

import com.example.employeeadministration.model.AggregateState
import com.example.employeeadministration.model.Employee
import com.example.employeeadministration.model.dto.EmployeeDto
import com.example.employeeadministration.model.dto.EmployeeSyncDto
import com.example.employeeadministration.repositories.EmployeeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

/**
 * Service for employee interactions.
 */
@Service
class EmployeeService(
        val employeeRepository: EmployeeRepository,
        val workflowService: WorkflowService,
        val objectMapper: ObjectMapper
) {

    fun saveEmployeeWithWorkflow(employee: Employee, compensationEmployee: Employee?): Employee {
        var variablesMap = emptyMap<String, String>()
        // If the employee was created no compensation data is necessary
        if (compensationEmployee != null) {
            variablesMap = variablesMap.plus("compensationEmployee" to objectMapper.writeValueAsString(mapToSyncDto(compensationEmployee)))
        }
        employee.state = AggregateState.PENDING
        val savedEmployee = employeeRepository.save(employee)
        variablesMap = variablesMap.plus("employee" to objectMapper.writeValueAsString(mapToSyncDto(savedEmployee)))
        workflowService.createWorkflowInstance(variablesMap, "synchronize-employee")
        return savedEmployee
    }

    fun mapToSyncDto(employee: Employee): EmployeeSyncDto {
        return EmployeeSyncDto(employee.id!!, employee.firstname, employee.lastname, employee.address, employee.mail, employee.iban, employee.department, employee.title, employee.hourlyRate, employee.deleted, employee.state)
    }

    fun mapToDto(employee: Employee): EmployeeDto {
        return EmployeeDto(employee.id!!, employee.firstname, employee.lastname, employee.address, employee.mail, employee.iban, employee.department, employee.title, employee.hourlyRate, employee.state)
    }

    fun getAllEmployees(): List<EmployeeDto> {
        return employeeRepository.findAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getEmployeesByDepartment(department: String): List<EmployeeDto> {
        return employeeRepository.findByDepartmentAndDeletedFalse(department).map { mapToDto(it) }
    }

    fun getEmployeeById(id: Long): EmployeeDto {
        return employeeRepository.findByIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    fun createEmployee(employeeDto: EmployeeDto): EmployeeDto {
        val employee = Employee(null, employeeDto.firstname, employeeDto.lastname, employeeDto.address, employeeDto.mail, employeeDto.iban, employeeDto.department, employeeDto.title, employeeDto.hourlyRate)
        return mapToDto(saveEmployeeWithWorkflow(employee, null))
    }

    /**
     * Compares received employee data with local employee data and applies updates.
     * The service does not just set the fields, it uses the aggregate functions to do so because these functions can contain business rules which have to be applied.
     * It is important to note that no updates are permitted without using aggregate functions.
     */
    fun updateEmployee(employeeDto: EmployeeDto): EmployeeDto {
        val employee = employeeRepository.findByIdAndDeletedFalse(employeeDto.id!!).orElseThrow()
        val compensationEmployee = employee.copy()
        if (employee.firstname != employeeDto.firstname || employee.lastname != employeeDto.lastname) {
            employee.changesName(employeeDto.firstname, employeeDto.lastname)
        }
        if (employee.address != employeeDto.address) {
            employee.moves(employeeDto.address)
        }
        if (employee.mail != employeeDto.mail) {
            employee.changesMail(employeeDto.mail)
        }
        if (employee.hourlyRate != employeeDto.hourlyRate) {
            employee.adjustRate(employeeDto.hourlyRate)
        }
        if (employee.department != employeeDto.department) {
            employee.movesToDepartment(employeeDto.department)
        }
        return mapToDto(saveEmployeeWithWorkflow(employee, compensationEmployee))
    }

    fun deleteEmployee(id: Long): EmployeeDto {
        val employee = employeeRepository.findByIdAndDeletedFalse(id).orElseThrow()
        val compensationEmployee = employee.copy()
        employee.deleteAggregate()
        return mapToDto(saveEmployeeWithWorkflow(employee, compensationEmployee))
    }

}