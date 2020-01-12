package com.example.worktimeadministration.controllers

import com.example.worktimeadministration.model.dto.ProjectDto
import com.example.worktimeadministration.services.project.ProjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

const val projectUrl = "project"

/**
 * REST API to interact with the replicated projects. Accepts no manipulating commands, only GET.
 */
@RestController
class ProjectController(val projectService: ProjectService) {

    @GetMapping(projectUrl)
    fun getAllProjects(): ResponseEntity<List<ProjectDto>> {
        return ResponseEntity.ok(projectService.getAllProjects())
    }

    @GetMapping("$projectUrl/{id}")
    fun getProjectById(@PathVariable("id") id: Long): ResponseEntity<ProjectDto> {
        try {
            return ResponseEntity.ok(projectService.getProjectById(id))
        } catch (ex: NoSuchElementException) {
            throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No Project under the given id", ex)
        }
    }

    @GetMapping("$projectUrl/employee/{id}")
    fun getProjectsOfEmployee(@PathVariable("id") employeeId: Long): ResponseEntity<List<ProjectDto>> {
        return ok(projectService.getProjectsOfEmployee(employeeId))
    }

}