package com.example.employeeadministration.services

import com.example.employeeadministration.model.aggregates.AggregateState
import com.example.employeeadministration.model.aggregates.Employee
import com.example.employeeadministration.model.dto.EmployeeDto
import com.example.employeeadministration.model.dto.EmployeeSync
import com.example.employeeadministration.repositories.DepartmentRepository
import com.example.employeeadministration.repositories.EmployeeRepository
import com.example.employeeadministration.repositories.PositionRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for employee interactions.
 */
@Service
class EmployeeService(
        val employeeRepository: EmployeeRepository,
        val departmentRepository: DepartmentRepository,
        val positionRepository: PositionRepository,
        val departmentService: DepartmentService,
        val positionService: PositionService,
        val workflowService: WorkflowService,
        val objectMapper: ObjectMapper
): WorkflowPersistenceService<Employee>, MappingService<Employee, EmployeeDto>, SyncMappingService<Employee, EmployeeSync> {

    @Transactional
    override fun saveAggregateWithWorkflow(aggregate: Employee, compensationAggregate: Employee?): Employee {
        var variablesMap = emptyMap<String, String>()
        // If the employee was created no compensation data is necessary
        if (compensationAggregate != null) {
            variablesMap = variablesMap.plus("compensationEmployee" to objectMapper.writeValueAsString(mapToSyncDto(compensationAggregate)))
        }
        aggregate.state = AggregateState.PENDING
        val savedEmployee = employeeRepository.save(aggregate)
        variablesMap = variablesMap.plus("employee" to objectMapper.writeValueAsString(mapToSyncDto(savedEmployee)))
        workflowService.createWorkflowInstance(variablesMap, "synchronize-employee")
        return savedEmployee
    }

    override fun mapToSyncDto(employee: Employee): EmployeeSync {
        return EmployeeSync(
                employee.id!!,
                employee.firstname,
                employee.lastname,
                employee.birthday,
                employee.address,
                employee.bankDetails,
                employee.department.id!!,
                employee.position.id!!,
                employee.hourlyRate,
                employee.companyMail,
                employee.availableVacationHours,
                employee.deleted,
                employee.state
        )
    }

    override fun mapToDto(employee: Employee): EmployeeDto {
        return EmployeeDto(
                employee.id!!,
                employee.firstname,
                employee.lastname,
                employee.birthday,
                employee.address,
                employee.bankDetails,
                departmentService.mapToDto(employee.department),
                positionService.mapToDto(employee.position),
                employee.hourlyRate,
                employee.availableVacationHours,
                employee.companyMail,
                employee.state)
    }

    override fun mapToEntity(dto: EmployeeDto): Employee {
        val department = departmentRepository.findById(dto.department.id!!).orElseThrow()
        val position = positionRepository.findById(dto.position.id!!).orElseThrow()
        return Employee(
                dto.id,
                dto.firstname,
                dto.lastname,
                dto.birthday,
                dto.address,
                dto.bankDetails,
                department,
                position,
                dto.availableVacationHours,
                dto.hourlyRate,
                dto.companyMail
        )
    }

    fun getAllEmployees(): List<EmployeeDto> {
        return employeeRepository.getAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getEmployeeById(id: Long): EmployeeDto {
        return employeeRepository.getByIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    fun getEmployeesByName(firstname: String, lastname: String): List<EmployeeDto> {
        return employeeRepository.getAllByFirstnameContainingAndLastnameContainingAndDeletedFalse(firstname, lastname).map {
            mapToDto(it)
        }
    }

    fun getEmployeesOfDepartment(departmentId: Long): List<EmployeeDto> {
        return employeeRepository.getAllByDepartment_IdAndDeletedFalse(departmentId).map { mapToDto(it) }
    }

    fun getEmployeesByPosition(positionId: Long): List<EmployeeDto> {
        return employeeRepository.getAllByPosition_IdAndDeletedFalse(positionId).map { mapToDto(it) }
    }

    @Transactional
    fun createEmployee(employeeDto: EmployeeDto): EmployeeDto {
        val employee = mapToEntity(employeeDto)
        return mapToDto(saveAggregateWithWorkflow(employee, null))
    }

    @Transactional
    fun updateEmployee(employeeDto: EmployeeDto): EmployeeDto {
        val employee = employeeRepository.findById(employeeDto.id!!).orElseThrow()
        if (employee.state == AggregateState.PENDING) throw RuntimeException("Employee is still pending")
        val compensationEmployee = employee.copy()
        if (employee.firstname != employeeDto.firstname || employee.lastname != employeeDto.lastname) {
            employee.changeName(employeeDto.firstname, employeeDto.lastname)
        }
        if (employee.address != employeeDto.address) {
            employee.moveToNewAddress(employeeDto.address)
        }
        if (employee.hourlyRate != employeeDto.hourlyRate) {
            employee.receiveRaiseBy(employee.hourlyRate.minus(employeeDto.hourlyRate))
        }
        if (employee.bankDetails != employeeDto.bankDetails) {
            employee.switchBankDetails(employeeDto.bankDetails)
        }
        if (employee.department.id!! != employeeDto.department.id!!) {
            employee.moveToAnotherDepartment(departmentRepository.findById(employeeDto.department.id).orElseThrow())
        }
        if (employee.position.id!! != employeeDto.position.id!!) {
            employee.changeJobPosition(positionRepository.findById(employeeDto.position.id).orElseThrow(), null)
        }
        return mapToDto(saveAggregateWithWorkflow(employee, compensationEmployee))
    }


    @Transactional
    fun deleteEmployee(id: Long) {
        val employee = employeeRepository.getByIdAndDeletedFalse(id).orElseThrow {
            Exception("The employee you are trying to delete does not exist")
        }
        if (employee.state == AggregateState.PENDING) throw RuntimeException("Employee is still pending")
        val compensationEmployee = employee.copy()
        employee.deleteAggregate()
        saveAggregateWithWorkflow(employee, compensationEmployee)
    }



}