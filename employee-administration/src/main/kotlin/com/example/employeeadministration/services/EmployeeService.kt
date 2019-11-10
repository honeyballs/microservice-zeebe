package com.example.employeeadministration.services

import com.example.employeeadministration.model.AggregateState
import com.example.employeeadministration.model.Employee
import com.example.employeeadministration.model.EmployeeDto
import com.example.employeeadministration.model.EmployeeSyncDto
import com.example.employeeadministration.repositories.EmployeeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeService(
        val employeeRepository: EmployeeRepository,
        val workflowService: WorkflowService,
        val objectMapper: ObjectMapper
) {

    fun saveEmployeeWithWorkflow(employee: Employee, operation: Operation): Employee {
        val savedEmployee = employeeRepository.save(employee)
        savedEmployee.state = AggregateState.PENDING
        val pair = "employee" to objectMapper.writeValueAsString(mapToSyncDto(employee))
        workflowService.createWorkflowInstance(mapOf(pair), "employee-${operation.value}")
        return savedEmployee
    }

    enum class Operation(val value: String) {
        CREATED("created"), UPDATED("updated"), DELETED("deleted")
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
        return mapToDto(saveEmployeeWithWorkflow(employee, Operation.CREATED))
    }

    fun updateEmployee(employeeDto: EmployeeDto): EmployeeDto {
        val employee = employeeRepository.findByIdAndDeletedFalse(employeeDto.id).orElseThrow()
        if (employee.firstname != employeeDto.firstname || employee.lastname != employeeDto.lastname) {
            employee.changesName(employeeDto.firstname, employeeDto.lastname)
        }
        if (employee.address != employeeDto.address) {
            employee.moves(employeeDto.address)
        }
        if (employee.hourlyRate != employeeDto.hourlyRate) {
            employee.adjustRate(employeeDto.hourlyRate)
        }
        if (employee.department != employeeDto.department) {
            employee.movesToDepartment(employeeDto.department)
        }
        return mapToDto(saveEmployeeWithWorkflow(employee, Operation.UPDATED))
    }

    fun deleteEmployee(id: Long): EmployeeDto {
        val employee = employeeRepository.findByIdAndDeletedFalse(id).orElseThrow()
        employee.deleteAggregate()
        return mapToDto(saveEmployeeWithWorkflow(employee, Operation.DELETED))
    }

}