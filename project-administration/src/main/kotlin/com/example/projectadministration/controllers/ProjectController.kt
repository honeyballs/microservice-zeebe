package com.example.projectadministration.controllers

import com.example.projectadministration.model.dto.ProjectDto
import com.example.projectadministration.services.ProjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

const val projectUrl = "project"

/**
 * REST API to interact with projects.
 */
@RestController
class ProjectController(val projectService: ProjectService) {

    @GetMapping(projectUrl)
    fun getAllProjects(): ResponseEntity<List<ProjectDto>> {
        return ok(projectService.getAllProjects())
    }

    @GetMapping("$projectUrl/{id}")
    fun getProjectById(@PathVariable("id") id: Long): ResponseEntity<ProjectDto> {
        try {
            return ok(projectService.getProjectById(id))
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No project found")
        }
    }

    @GetMapping("$projectUrl/customer/{id}")
    fun getProjectsOfCustomer(@PathVariable("id") customerId: Long): ResponseEntity<List<ProjectDto>> {
        return ok(projectService.getProjectsOfCustomer(customerId))
    }

    @GetMapping("$projectUrl/employee/{id}")
    fun getProjectsOfEmployee(@PathVariable("id") employeeId: Long): ResponseEntity<List<ProjectDto>> {
        try {
            return ok(projectService.getProjectsOfEmployee(employeeId))
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No employee found")
        }
    }

    @PostMapping(projectUrl)
    fun createProject(@RequestBody projectDto: ProjectDto): ResponseEntity<ProjectDto> {
        try {
            return ok(projectService.createProject(projectDto))
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Project could not be created", ex)
        }
    }

    @PutMapping(projectUrl)
    fun updateProject(@RequestBody projectDto: ProjectDto): ResponseEntity<ProjectDto> {
        try {
            return ok(projectService.updateProject(projectDto))
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Project could not be updated", ex)
        }
    }

    @DeleteMapping("$projectUrl/{id}")
    fun deleteProject(@PathVariable("id") id: Long) {
        try {
            projectService.deleteProject(id)
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Project could not be deleted", ex)
        }
    }

}