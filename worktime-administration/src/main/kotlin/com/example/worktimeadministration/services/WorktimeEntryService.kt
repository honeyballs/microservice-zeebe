package com.example.worktimeadministration.services

import com.example.worktimeadministration.model.WorktimeEntry
import com.example.worktimeadministration.model.dto.WorktimeEntryDto
import com.example.worktimeadministration.repositories.WorktimeEmployeeRepository
import com.example.worktimeadministration.repositories.WorktimeEntryRepository
import com.example.worktimeadministration.repositories.WorktimeProjectRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class WorktimeEntryService(
        val worktimeEntryRepository: WorktimeEntryRepository,
        val workflowService: WorkflowService,
        val mapper: ObjectMapper,
        val employeeService: WorktimeEmployeeService,
        val projectService: WorktimeProjectService,
        val projectRepository: WorktimeProjectRepository,
        val employeeRepository: WorktimeEmployeeRepository
) {

    fun mapToDto(worktimeEntry: WorktimeEntry): WorktimeEntryDto {
        return WorktimeEntryDto(
                worktimeEntry.id!!,
                worktimeEntry.startTime,
                worktimeEntry.endTime,
                worktimeEntry.pauseTimeInMinutes,
                projectService.mapToDto(worktimeEntry.project),
                employeeService.mapToDto(worktimeEntry.employee),
                worktimeEntry.description,
                worktimeEntry.state
        )
    }

    fun getAll(): List<WorktimeEntryDto> {
        return worktimeEntryRepository.findAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getById(id: Long): WorktimeEntryDto {
        return worktimeEntryRepository.findByIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    fun getByEmployee(employeeId: Long): List<WorktimeEntryDto> {
        return worktimeEntryRepository.findAllByEmployee_EmployeeIdAndDeletedFalse(employeeId).map { mapToDto(it) }
    }

    fun getByProject(projectId: Long): List<WorktimeEntryDto> {
        return worktimeEntryRepository.findAllByProject_ProjectIdAndDeletedFalse(projectId).map { mapToDto(it) }
    }

    fun createWorktimeEntry(worktimeEntryDto: WorktimeEntryDto): WorktimeEntryDto {
        val project = projectRepository.findById(worktimeEntryDto.project.id).orElseThrow()
        val employee = employeeRepository.findById(worktimeEntryDto.employee.id).orElseThrow()
        val entry = WorktimeEntry(
                null,
                worktimeEntryDto.startTime,
                worktimeEntryDto.endTime,
                worktimeEntryDto.pauseTimeInMinutes,
                project,
                employee,
                worktimeEntryDto.description
        )
        return mapToDto(worktimeEntryRepository.save(entry))
    }

    fun updateWorktimeEntry(worktimeEntryDto: WorktimeEntryDto): WorktimeEntryDto {
        val entry = worktimeEntryRepository.findByIdAndDeletedFalse(worktimeEntryDto.id!!).orElseThrow()
        if (entry.startTime != worktimeEntryDto.startTime) {
            entry.adjustStartTime(worktimeEntryDto.startTime)
        }
        if (entry.endTime != worktimeEntryDto.endTime) {
            entry.adjustEndTime(worktimeEntryDto.endTime)
        }
        if (entry.pauseTimeInMinutes != worktimeEntryDto.pauseTimeInMinutes) {
            entry.adjustPause(worktimeEntryDto.pauseTimeInMinutes)
        }
        if (entry.description != worktimeEntryDto.description) {
            entry.changeDescription(worktimeEntryDto.description)
        }
        if (entry.project.projectId != worktimeEntryDto.project.id) {
            entry.changeProject(projectRepository.findByProjectIdAndDeletedFalse(worktimeEntryDto.project.id).orElseThrow())
        }
        return mapToDto(worktimeEntryRepository.save(entry))
    }

    fun deleteWorktimeEntry(id: Long): WorktimeEntryDto {
        val entry = worktimeEntryRepository.findByIdAndDeletedFalse(id).orElseThrow()
        entry.deleteAggregate()
        return mapToDto(worktimeEntryRepository.save(entry))
    }

}