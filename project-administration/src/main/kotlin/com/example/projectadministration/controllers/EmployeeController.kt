package com.example.projectadministration.controllers

import com.example.projectadministration.model.dto.EmployeeDto
import com.example.projectadministration.services.employee.EmployeeService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

const val employeeUrl = "employee"

/**
 * REST API to interact with the replicated employees. Accepts no manipulating commands, only GET.
 */
@RestController
class ProjectEmployeeController(val employeeService: EmployeeService) {

    @GetMapping(employeeUrl)
    fun getAllEmployees(): ResponseEntity<List<EmployeeDto>> {
        return ok(employeeService.getAllEmployees())
    }

    @GetMapping("$employeeUrl/{id}")
    fun getEmployeeById(@PathVariable("id") id: Long): ResponseEntity<EmployeeDto> {
        return ok(employeeService.getEmployeeById(id))
    }

    @GetMapping("$employeeUrl/department/{name}")
    fun getAllEmployeesInDepartment(@PathVariable("name") name: String): ResponseEntity<List<EmployeeDto>> {
        return ok(employeeService.getAllEmployeesInDepartment(name))
    }

    @GetMapping("$employeeUrl/position/{title}")
    fun getAllEmployeesOfPosition(@PathVariable("title") title: String): ResponseEntity<List<EmployeeDto>> {
        return ok(employeeService.getAllEmployeesInPosition(title))
    }

    @GetMapping("$employeeUrl/project/{id}")
    fun getEmployeesOfProject(@PathVariable("id") projectId: Long): ResponseEntity<List<EmployeeDto>> {
        return ok(employeeService.getAllEmployeesOfProject(projectId))
    }

}