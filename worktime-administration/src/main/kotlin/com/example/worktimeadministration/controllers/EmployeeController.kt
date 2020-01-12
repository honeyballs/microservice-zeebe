package com.example.worktimeadministration.controllers

import com.example.worktimeadministration.model.dto.EmployeeDto
import com.example.worktimeadministration.services.employee.EmployeeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

const val employeeUrl = "employee"

/**
 * REST API to interact with the replicated employees. Accepts no manipulating commands, only GET.
 */
@RestController
class EmployeeController(val employeeService: EmployeeService) {

    @GetMapping(employeeUrl)
    fun getAllEmployees(): ResponseEntity<List<EmployeeDto>> {
        return ok(employeeService.getAllEmployees())
    }

    @GetMapping("$employeeUrl/{id}")
    fun getEmployeeById(@PathVariable("id") id: Long): ResponseEntity<EmployeeDto> {
        try {
            return ok(employeeService.getEmployeeById(id))
        } catch (ex: NoSuchElementException) {
            throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No Employee under the given id", ex)
        }
    }

    @GetMapping("$employeeUrl/project/{id}")
    fun getEmployeesOfProject(@PathVariable("id") projectId: Long): ResponseEntity<List<EmployeeDto>> {
        return ok(employeeService.getEmployeesOfProject(projectId))
    }

}