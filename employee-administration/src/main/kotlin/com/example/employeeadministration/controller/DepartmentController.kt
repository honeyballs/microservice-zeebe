package com.example.employeeadministration.controller

import com.example.employeeadministration.model.dto.DepartmentDto
import com.example.employeeadministration.repositories.DepartmentRepository
import com.example.employeeadministration.services.DepartmentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

const val departmentUrl = "department"

@RestController
class DepartmentController(val departmentService: DepartmentService) {

    @GetMapping(departmentUrl)
    fun getAllDepartments(): ResponseEntity<List<DepartmentDto>> {
        return ok(departmentService.getAllDepartments())
    }

    @GetMapping("$departmentUrl/{id}")
    fun getDepartmentById(@PathVariable("id") id: Long): ResponseEntity<DepartmentDto> {
        try {
            return ok(departmentService.getDepartmentById(id))
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not find department using the given id")
        }
    }

    @PostMapping(departmentUrl)
    fun createDepartment(@RequestBody departmentDto: DepartmentDto): ResponseEntity<DepartmentDto> {
        return ok(departmentService.createDepartment(departmentDto))
    }

    @PutMapping(departmentUrl)
    fun updateDepartment(@RequestBody departmentDto: DepartmentDto): ResponseEntity<DepartmentDto> {
        try {
            return ok(departmentService.updateDepartment(departmentDto))
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong when updating the department", ex)
        }
    }

    @DeleteMapping("$departmentUrl/{id}")
    fun deleteDepartment(@PathVariable("id") id: Long) {
        try {
            departmentService.deleteDepartment(id)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
}