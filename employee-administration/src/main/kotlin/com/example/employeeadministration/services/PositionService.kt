package com.example.employeeadministration.services

import com.example.employeeadministration.model.aggregates.AggregateState
import com.example.employeeadministration.model.aggregates.Position
import com.example.employeeadministration.model.dto.PositionDto
import com.example.employeeadministration.model.dto.PositionSync
import com.example.employeeadministration.repositories.EmployeeRepository
import com.example.employeeadministration.repositories.PositionRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PositionService(
        val positionRepository: PositionRepository,
        val workflowService: WorkflowService,
        val employeeRepository: EmployeeRepository,
        val objectMapper: ObjectMapper
): WorkflowPersistenceService<Position>, MappingService<Position, PositionDto>, SyncMappingService<Position, PositionSync> {

    override fun saveAggregateWithWorkflow(aggregate: Position, compensationAggregate: Position?): Position {
        var variablesMap = emptyMap<String, String>()
        // If the employee was created no compensation data is necessary
        if (compensationAggregate != null) {
            variablesMap = variablesMap.plus("compensationPosition" to objectMapper.writeValueAsString(mapToSyncDto(compensationAggregate)))
        }
        aggregate.state = AggregateState.PENDING
        val savedPosition = positionRepository.save(aggregate)
        variablesMap = variablesMap.plus("position" to objectMapper.writeValueAsString(mapToSyncDto(savedPosition)))
        workflowService.createWorkflowInstance(variablesMap, "synchronize-position")
        return savedPosition
    }

    override fun mapToDto(aggregate: Position): PositionDto {
        return PositionDto(aggregate.id!!, aggregate.title, aggregate.minHourlyWage, aggregate.maxHourlyWage, aggregate.state)
    }

    override fun mapToEntity(dto: PositionDto): Position {
        return Position(dto.id, dto.title, dto.minHourlyWage, dto.maxHourlyWage)
    }

    override fun mapToSyncDto(aggregate: Position): PositionSync {
        return PositionSync(aggregate.id!!, aggregate.title, aggregate.minHourlyWage, aggregate.maxHourlyWage, aggregate.deleted, aggregate.state)
    }

    fun getAllPositions(): List<PositionDto> {
        return positionRepository.getAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getPositionById(id: Long): PositionDto {
        return positionRepository.getByIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    /**
     * Return a position if present, otherwise save a new position.
     */
    @Transactional
    fun createPosition(positionDto: PositionDto): PositionDto {
        return positionRepository.getByTitleAndDeletedFalse(positionDto.title)
                .map { mapToDto(it) }
                .orElseGet { mapToDto(saveAggregateWithWorkflow(mapToEntity(positionDto), null)) }
    }

    @Transactional
    fun updatePosition(positionDto: PositionDto): PositionDto {
        val position = positionRepository.findById(positionDto.id!!).orElseThrow()
        val compensationPosition = position.copy()
        if (position.state == AggregateState.PENDING) throw RuntimeException("Position is still pending")
        if (position.title != positionDto.title) {
            position.changePositionTitle(positionDto.title)
        }
        if (position.minHourlyWage != positionDto.minHourlyWage || position.maxHourlyWage != positionDto.maxHourlyWage) {
            position.adjustWageRange(positionDto.minHourlyWage, positionDto.maxHourlyWage)
        }
        return mapToDto(saveAggregateWithWorkflow(position, compensationPosition))
    }

    @Transactional
    fun deletePosition(id: Long) {
        if (employeeRepository.getAllByPosition_IdAndDeletedFalse(id).isEmpty()) {
            val position = positionRepository.getByIdAndDeletedFalse(id).orElseThrow {
                Exception("The job position you are trying to delete does not exist")
            }
            if (position.state == AggregateState.PENDING) throw RuntimeException("Position is still pending")
            val compensationPosition = position.copy()
            position.deleteAggregate()
            saveAggregateWithWorkflow(position, compensationPosition)
        } else {
            throw Exception("The job position has employees assigned to it and cannot be deleted.")
        }
    }
}