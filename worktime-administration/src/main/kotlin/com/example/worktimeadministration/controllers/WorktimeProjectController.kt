package com.example.worktimeadministration.controllers

import com.example.worktimeadministration.model.dto.WorktimeProjectDto
import com.example.worktimeadministration.services.WorktimeProjectService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

const val projectUrl = "projects"

/**
 * REST API to interact with the replicated projects. Accepts no manipulating commands, only GET.
 */
@RestController
class WorktimeProjectController(val projectService: WorktimeProjectService) {

    @GetMapping(projectUrl)
    fun getAllProjects(): ResponseEntity<List<WorktimeProjectDto>> {
        return ok(projectService.getAllProjects())
    }

    @GetMapping("$projectUrl/running")
    fun getAllRunningProjects(): ResponseEntity<List<WorktimeProjectDto>> {
        return ok(projectService.getAllUnfinishedProjects())
    }

    @GetMapping("$projectUrl/{id}")
    fun getProjectById(@PathVariable("id") id: Long): ResponseEntity<WorktimeProjectDto> {
        return ok(projectService.getById(id))
    }

}