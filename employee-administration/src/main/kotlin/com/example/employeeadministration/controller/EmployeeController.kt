package com.example.employeeadministration.controller

import com.example.employeeadministration.model.dto.EmployeeDto
import com.example.employeeadministration.services.EmployeeService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*

const val url = "employees"

/**
 * REST API to interact with employees.
 */
@RestController
class EmployeeController(val employeeService: EmployeeService) {

    @GetMapping(url)
    fun getAllEmployees(): ResponseEntity<List<EmployeeDto>> {
        return ok(employeeService.getAllEmployees())
    }

    @GetMapping("$url/department/{department}")
    fun getAllEmployeesOfDepartment(@PathVariable("department") department: String): ResponseEntity<List<EmployeeDto>> {
        return ok(employeeService.getEmployeesByDepartment(department))
    }

    @GetMapping("$url/{id}")
    fun getEmployeeById(@PathVariable("id") id: Long): ResponseEntity<EmployeeDto> {
        return ok(employeeService.getEmployeeById(id))
    }

    @PostMapping(url)
    fun createEmployee(@RequestBody employeeDto: EmployeeDto): ResponseEntity<EmployeeDto> {
        return ok(employeeService.createEmployee(employeeDto))
    }

    @PutMapping(url)
    fun updateEmployee(@RequestBody employeeDto: EmployeeDto): ResponseEntity<EmployeeDto> {
        return ok(employeeService.updateEmployee(employeeDto))
    }

    @DeleteMapping("$url/{id}")
    fun updateEmployee(@PathVariable("id") id: Long): ResponseEntity<EmployeeDto> {
        return ok(employeeService.deleteEmployee(id))
    }

}