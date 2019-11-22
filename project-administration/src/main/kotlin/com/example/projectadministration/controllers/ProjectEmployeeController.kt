package com.example.projectadministration.controllers

import com.example.projectadministration.model.dto.ProjectEmployeeDto
import com.example.projectadministration.services.ProjectEmployeeService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

const val employeeUrl = "employees"

/**
 * REST API to interact with the replicated employees. Accepts no manipulating commands, only GET.
 */
@RestController
class ProjectEmployeeController(val projectEmployeeService: ProjectEmployeeService) {

    @GetMapping(employeeUrl)
    fun getAllEmployees(): ResponseEntity<List<ProjectEmployeeDto>> {
        return ok(projectEmployeeService.getAllEmployees())
    }

    @GetMapping("$employeeUrl/{id}")
    fun getEmployeeById(@PathVariable("id") id: Long): ResponseEntity<ProjectEmployeeDto> {
        return ok(projectEmployeeService.getEmployeeById(id))
    }

    @GetMapping("$employeeUrl/department/{department}")
    fun getEmployeesByDepartment(@PathVariable("department") department: String): ResponseEntity<List<ProjectEmployeeDto>> {
        return ok(projectEmployeeService.getEmployeesByDepartment(department))
    }

}