package com.example.worktimeadministration.services

import com.example.worktimeadministration.model.dto.WorktimeProjectDto
import com.example.worktimeadministration.model.project.WorktimeProject
import com.example.worktimeadministration.repositories.WorktimeProjectRepository
import org.springframework.stereotype.Service

@Service
class WorktimeProjectService(val projectRepository: WorktimeProjectRepository) {

    fun mapToDto(project: WorktimeProject): WorktimeProjectDto {
        return WorktimeProjectDto(project.projectId, project.name, project.startDate, project.endDate, project.state)
    }

    fun getAllProjects(): List<WorktimeProjectDto> {
        return projectRepository.findAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getById(id: Long): WorktimeProjectDto {
        return projectRepository.findByProjectIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    fun getAllUnfinishedProjects(): List<WorktimeProjectDto> {
        return projectRepository.findByEndDateNullAndDeletedFalse().map { mapToDto(it) }
    }

}