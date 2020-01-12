package com.example.employeeadministration.controller

import com.example.employeeadministration.model.aggregates.Employee
import com.example.employeeadministration.model.dto.EmployeeDto
import com.example.employeeadministration.repositories.DepartmentRepository
import com.example.employeeadministration.repositories.EmployeeRepository
import com.example.employeeadministration.repositories.PositionRepository
import com.example.employeeadministration.services.EmployeeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

const val employeeUrl = "employee"

@RestController
class EmployeeController(
        val service: EmployeeService
) {

    @GetMapping(employeeUrl)
    fun getAllEmployees(): ResponseEntity<List<EmployeeDto>> {
        return ok(service.getAllEmployees())
    }

    @GetMapping("$employeeUrl/{id}")
    fun getEmployeeById(@PathVariable("id") id: Long): ResponseEntity<EmployeeDto> {
        try {
            return ok(service.getEmployeeById(id))
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not find employee using the given id")

        }
    }

    @GetMapping("$employeeUrl/department/{depId}")
    fun getEmployeesOfDepartment(@PathVariable("depId") departmentId: Long): ResponseEntity<List<EmployeeDto>> {
        return ok(service.getEmployeesOfDepartment(departmentId))
    }

    @PostMapping("$employeeUrl/position/{posId}")
    fun getEmployeesByPosition(@PathVariable("posId") positionId: Long): ResponseEntity<List<EmployeeDto>> {
        return ok(service.getEmployeesByPosition(positionId))
    }

    @GetMapping("$employeeUrl/name")
    fun getEmployeesByName(@RequestParam("firstname") firstname: String, @RequestParam("lastname") lastname: String): ResponseEntity<List<EmployeeDto>> {
        return ok(service.getEmployeesByName(firstname, lastname))
    }

    @PostMapping(employeeUrl)
    fun createEmployee(@RequestBody employeeDto: EmployeeDto): ResponseEntity<EmployeeDto> {
        return ok(service.createEmployee(employeeDto))
    }

    @PutMapping(employeeUrl)
    fun updateEmployee(@RequestBody employeeDto: EmployeeDto): ResponseEntity<EmployeeDto> {
        try {
            return ok(service.updateEmployee(employeeDto))
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong when updating the employee", ex)
        }
    }

    @DeleteMapping("$employeeUrl/{id}")
    fun deleteEmployee(@PathVariable("id") id: Long) {
        try {
            service.deleteEmployee(id)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

}