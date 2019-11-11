package com.example.projectadministration.controllers

import com.example.projectadministration.model.dto.ProjectDto
import com.example.projectadministration.services.ProjectService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*

const val projectUrl = "projects"

@RestController
class ProjectController(val projectService: ProjectService) {

    @GetMapping(projectUrl)
    fun getAllProjects(): ResponseEntity<List<ProjectDto>> {
        return ok(projectService.getAllProjects())
    }

    @GetMapping("$projectUrl/{id}")
    fun getProjectById(@PathVariable("id") id: Long): ResponseEntity<ProjectDto> {
        return ok(projectService.getProjectById(id))
    }

    @GetMapping("$projectUrl/finished")
    fun getAllFinishedProjects(): ResponseEntity<List<ProjectDto>> {
        return ok(projectService.getFinishedProjects())
    }

    @GetMapping("$projectUrl/employee/{employeeId}")
    fun getAllFinishedProjects(@PathVariable("employeeId") id: Long): ResponseEntity<List<ProjectDto>> {
        return ok(projectService.getProjectsOfEmployee(id))
    }

    @PostMapping(projectUrl)
    fun createProject(@RequestBody projectDto: ProjectDto): ResponseEntity<ProjectDto> {
        return ok(projectService.createProject(projectDto))
    }

    @PutMapping(projectUrl)
    fun updateProject(@RequestBody projectDto: ProjectDto): ResponseEntity<ProjectDto> {
        return ok(projectService.updateProject(projectDto))
    }

    @DeleteMapping("$projectUrl/{id}")
    fun deleteMapping(@PathVariable("id") id: Long): ResponseEntity<ProjectDto> {
        return ok(projectService.deleteProject(id))
    }

}