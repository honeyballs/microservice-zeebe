package com.example.worktimeadministration.controllers

import com.example.worktimeadministration.model.dto.WorktimeEmployeeDto
import com.example.worktimeadministration.model.employee.WorktimeEmployee
import com.example.worktimeadministration.services.WorktimeEmployeeService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

const val employeeUrl = "employees"

@RestController
class WorktimeEmployeeController(val employeeService: WorktimeEmployeeService) {

    @GetMapping(employeeUrl)
    fun getAllEmployees(): ResponseEntity<List<WorktimeEmployeeDto>> {
        return ok(employeeService.getAllEmployees())
    }

    @GetMapping("$employeeUrl/{id}")
    fun getEmployeeById(@PathVariable("id") id: Long): ResponseEntity<WorktimeEmployeeDto> {
        return ok(employeeService.getById(id))
    }

}