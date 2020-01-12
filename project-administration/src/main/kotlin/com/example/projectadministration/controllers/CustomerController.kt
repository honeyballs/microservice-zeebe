package com.example.projectadministration.controllers

import com.example.projectadministration.model.dto.CustomerDto
import com.example.projectadministration.services.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

const val customerUrl = "customer"

@RestController
class CustomerController(val customerService: CustomerService) {

    @GetMapping(customerUrl)
    fun getAllCustomers(): ResponseEntity<List<CustomerDto>> {
        return ok(customerService.getAllCustomers())
    }

    @GetMapping("$customerUrl/{id}")
    fun getCustomerById(@PathVariable("id") id: Long): ResponseEntity<CustomerDto> {
        try {
            return ok(customerService.getCustomerById(id))
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No customer found")
        }
    }

    @GetMapping("$customerUrl/project/{id}")
    fun getCustomerOfProject(@PathVariable("id") projectId: Long): ResponseEntity<CustomerDto> {
        try {
            return ok(customerService.getCustomerOfProject(projectId))
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No customer found")
        }
    }

    @PostMapping(customerUrl)
    fun createCustomer(@RequestBody customerDto: CustomerDto): ResponseEntity<CustomerDto> {
        try {
            return ResponseEntity.ok(customerService.createCustomer(customerDto))
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create customer", ex)
        }
    }

    @PutMapping(customerUrl)
    fun updateCustomer(customerDto: CustomerDto): ResponseEntity<CustomerDto> {
        try {
            return ResponseEntity.ok(customerService.updateCustomer(customerDto))
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not update customer", ex)
        }
    }

    @DeleteMapping("$customerUrl/{id}")
    fun deleteCustomer(@PathVariable("id") id: Long) {
        try {
            customerService.deleteCustomer(id)
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete customer", ex)
        }
    }

}