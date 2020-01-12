package com.example.employeeadministration.services

import com.example.employeeadministration.model.aggregates.AggregateState
import com.example.employeeadministration.model.aggregates.Department
import com.example.employeeadministration.model.dto.DepartmentDto
import com.example.employeeadministration.model.dto.DepartmentSync
import com.example.employeeadministration.repositories.DepartmentRepository
import com.example.employeeadministration.repositories.EmployeeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DepartmentService(
        val departmentRepository: DepartmentRepository,
        val employeeRepository: EmployeeRepository,
        val workflowService: WorkflowService,
        val objectMapper: ObjectMapper
): WorkflowPersistenceService<Department>, MappingService<Department, DepartmentDto>, SyncMappingService<Department, DepartmentSync> {

    @Transactional
    override fun saveAggregateWithWorkflow(aggregate: Department, compensationAggregate: Department?): Department {
        var variablesMap = emptyMap<String, String>()
        // If the employee was created no compensation data is necessary
        if (compensationAggregate != null) {
            variablesMap = variablesMap.plus("compensationDepartment" to objectMapper.writeValueAsString(mapToSyncDto(compensationAggregate)))
        }
        aggregate.state = AggregateState.PENDING
        val savedDepartment = departmentRepository.save(aggregate)
        variablesMap = variablesMap.plus("department" to objectMapper.writeValueAsString(mapToSyncDto(savedDepartment)))
        workflowService.createWorkflowInstance(variablesMap, "synchronize-department")
        return savedDepartment
    }

    override fun mapToDto(aggregate: Department): DepartmentDto {
        return DepartmentDto(aggregate.id!!, aggregate.name, aggregate.state)
    }

    override fun mapToEntity(dto: DepartmentDto): Department {
        return Department(dto.id, dto.name)
    }

    override fun mapToSyncDto(aggregate: Department): DepartmentSync {
        return DepartmentSync(aggregate.id!!, aggregate.name, aggregate.deleted, aggregate.state)
    }

    fun getAllDepartments(): List<DepartmentDto> {
        return departmentRepository.getAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getDepartmentById(id: Long): DepartmentDto {
        return departmentRepository.getByIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    @Transactional
    fun createDepartment(departmentDto: DepartmentDto): DepartmentDto {
        return departmentRepository.getByNameAndDeletedFalse(departmentDto.name)
                .map { mapToDto(it) }
                .orElseGet { mapToDto(saveAggregateWithWorkflow(mapToEntity(departmentDto), null)) }
    }

    @Transactional
    fun updateDepartment(departmentDto: DepartmentDto): DepartmentDto {
        val department = departmentRepository.findById(departmentDto.id!!).orElseThrow()
        val compensationDepartment = department.copy()
        if (department.state == AggregateState.PENDING) throw RuntimeException("Department is still pending")
        if (department.name != departmentDto.name) {
            department.renameDepartment(departmentDto.name)
        }
        return mapToDto(saveAggregateWithWorkflow(department, compensationDepartment))
    }

    @Transactional
    fun deleteDepartment(id: Long) {
        if (employeeRepository.getAllByDepartment_IdAndDeletedFalse(id).isEmpty()) {
            val department = departmentRepository.getByIdAndDeletedFalse(id).orElseThrow {
                Exception("The department you are trying to delete does not exist")
            }
            if (department.state == AggregateState.PENDING) throw RuntimeException("Department is still pending")
            val compensationDepartment = department.copy()
            department.deleteAggregate()
            saveAggregateWithWorkflow(department, compensationDepartment)
        } else {
            throw Exception("The department has employees assigned to it and cannot be deleted.")
        }
    }

}