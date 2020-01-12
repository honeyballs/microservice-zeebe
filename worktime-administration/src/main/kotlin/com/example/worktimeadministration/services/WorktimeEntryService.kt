package com.example.worktimeadministration.services

import com.example.worktimeadministration.model.aggregates.AggregateState
import com.example.worktimeadministration.model.aggregates.EntryType
import com.example.worktimeadministration.model.aggregates.WorktimeEntry
import com.example.worktimeadministration.model.dto.WorktimeEntryDto
import com.example.worktimeadministration.model.dto.WorktimeEntrySync
import com.example.worktimeadministration.repositories.WorktimeEntryRepository
import com.example.worktimeadministration.repositories.employee.EmployeeRepository
import com.example.worktimeadministration.repositories.project.ProjectRepository
import com.example.worktimeadministration.services.employee.EmployeeService
import com.example.worktimeadministration.services.project.ProjectService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for worktime interactions.
 */
@Service
@Transactional
class WorktimeEntryService(
        val worktimeEntryRepository: WorktimeEntryRepository,
        val employeeRepository: EmployeeRepository,
        val projectRepository: ProjectRepository,
        val employeeService: EmployeeService,
        val projectService: ProjectService,
        val workflowService: WorkflowService,
        val mapper: ObjectMapper
): WorkflowPersistenceService<WorktimeEntry>, SyncMappingService<WorktimeEntry, WorktimeEntrySync>, MappingService<WorktimeEntry, WorktimeEntryDto> {

    override fun saveAggregateWithWorkflow(aggregate: WorktimeEntry, compensationAggregate: WorktimeEntry?): WorktimeEntry {
        // Only implemented to ease further development
        aggregate.state = AggregateState.ACTIVE
        return worktimeEntryRepository.save(aggregate)
    }

    override fun mapToSyncDto(aggregate: WorktimeEntry): WorktimeEntrySync {
        return WorktimeEntrySync(
                aggregate.id!!,
                aggregate.startTime,
                aggregate.endTime,
                aggregate.pauseTimeInMinutes,
                aggregate.project.projectId,
                aggregate.employee.employeeId,
                aggregate.description,
                aggregate.type,
                aggregate.deleted,
                aggregate.state
        )
    }

    override fun mapToDto(worktimeEntry: WorktimeEntry): WorktimeEntryDto {
        return WorktimeEntryDto(
                worktimeEntry.id!!,
                worktimeEntry.startTime,
                worktimeEntry.endTime,
                worktimeEntry.pauseTimeInMinutes,
                projectService.mapToDto(worktimeEntry.project),
                employeeService.mapToDto(worktimeEntry.employee),
                worktimeEntry.description,
                worktimeEntry.type,
                worktimeEntry.state
        )
    }

    override fun mapToEntity(dto: WorktimeEntryDto): WorktimeEntry {
        val project = projectRepository.findByProjectIdAndDeletedFalse(dto.project.id).orElseThrow()
        val employee = employeeRepository.findByEmployeeIdAndDeletedFalse(dto.employee.id).orElseThrow()
        return WorktimeEntry(dto.id, dto.startTime, dto.endTime, dto.pauseTimeInMinutes, project, employee, dto.description, dto.type)

    }

    fun getAllEntries(): List<WorktimeEntryDto> {
        return worktimeEntryRepository.findAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getEntryById(id: Long): WorktimeEntryDto {
        return worktimeEntryRepository.findByIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    fun getAllEntriesOfEmployee(employeeId: Long): List<WorktimeEntryDto> {
        return worktimeEntryRepository.findAllByEmployeeEmployeeIdAndDeletedFalse(employeeId).map { mapToDto(it) }
    }

    fun getAllEntriesOfProject(projectId: Long): List<WorktimeEntryDto> {
        return worktimeEntryRepository.findAllByProjectProjectIdAndDeletedFalse(projectId).map { mapToDto(it) }
    }

    fun getAllEntriesOfEmployeeOnProject(employeeId: Long, projectId: Long): List<WorktimeEntryDto> {
        return worktimeEntryRepository.findAllByProjectProjectIdAndEmployeeEmployeeIdAndDeletedFalse(projectId, employeeId).map { mapToDto(it) }
    }

    fun getHoursOnProject(projectId: Long): Int {
        val entries = worktimeEntryRepository.findAllByProjectProjectIdAndDeletedFalse(projectId)
        return entries.fold(0) { acc, worktimeEntry -> acc + worktimeEntry.calculateTimespan(worktimeEntry.startTime, worktimeEntry.endTime) }
    }

    /**
     * Collects multiple Updates on an aggregate.
     */
    fun updateWorktimeEntry(worktimeEntryDto: WorktimeEntryDto): WorktimeEntryDto {
        val entryToUpdate = worktimeEntryRepository.findById(worktimeEntryDto.id!!).orElseThrow()
        if (entryToUpdate.state == AggregateState.PENDING) throw RuntimeException("Entry is still pending")
        val compensationEntry = entryToUpdate.copy()
        if (entryToUpdate.startTime != worktimeEntryDto.startTime) {
            entryToUpdate.adjustStartTime(worktimeEntryDto.startTime)
        }
        if (entryToUpdate.endTime != worktimeEntryDto.endTime) {
            entryToUpdate.adjustEndTime(worktimeEntryDto.endTime)
        }
        if (entryToUpdate.pauseTimeInMinutes != worktimeEntryDto.pauseTimeInMinutes) {
            entryToUpdate.adjustPauseTime(worktimeEntryDto.pauseTimeInMinutes)
        }
        if (entryToUpdate.description != worktimeEntryDto.description) {
            entryToUpdate.changeDescription(worktimeEntryDto.description)
        }
        if (entryToUpdate.project.projectId != worktimeEntryDto.project.id) {
            val project = projectRepository.findByProjectIdAndDeletedFalse(worktimeEntryDto.project.id).orElseThrow()
            entryToUpdate.project = project
        }
        return mapToDto(saveAggregateWithWorkflow(entryToUpdate, compensationEntry))
    }

    fun createEntry(worktimeEntryDto: WorktimeEntryDto): WorktimeEntryDto {
        val entry = mapToEntity(worktimeEntryDto)
        if (entry.type == EntryType.VACATION) {
            entry.employee.usedVacationHours -= entry.calculateTimespan(entry.startTime, entry.endTime)
            employeeRepository.save(entry.employee)
        }
        return mapToDto(saveAggregateWithWorkflow(entry, null))
    }

    fun deleteEntry(id: Long) {
        val entry = worktimeEntryRepository.findById(id).orElseThrow()
        if (entry.state == AggregateState.PENDING) throw RuntimeException("Entry is still pending")
        val compensationEntry = entry.copy()
        entry.deleteAggregate()
        saveAggregateWithWorkflow(entry, compensationEntry)
    }

}