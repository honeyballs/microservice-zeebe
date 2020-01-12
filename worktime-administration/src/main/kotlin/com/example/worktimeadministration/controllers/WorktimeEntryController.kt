package com.example.worktimeadministration.controllers

import com.example.worktimeadministration.model.dto.WorktimeEntryDto
import com.example.worktimeadministration.repositories.WorktimeEntryRepository
import com.example.worktimeadministration.services.WorktimeEntryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

const val worktimeUrl = "worktime"

/**
 * REST API to interact with worktime entries.
 */
@RestController
class WorktimeEntryController(val worktimeEntryService: WorktimeEntryService, val worktimeEntryRepository: WorktimeEntryRepository) {

    @GetMapping(worktimeUrl)
    fun getAllEntries(): ResponseEntity<List<WorktimeEntryDto>> {
        return ResponseEntity.ok(worktimeEntryService.getAllEntries())
    }

    @GetMapping("$worktimeUrl/{id}")
    fun getEntryById(@PathVariable("id") id: Long): ResponseEntity<WorktimeEntryDto> {
        try {
            return ResponseEntity.ok(worktimeEntryService.getEntryById(id))
        } catch (ex: NoSuchElementException) {
            throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No Entry under the given id", ex)
        }
    }

    @GetMapping("$worktimeUrl/employee/{id}")
    fun getAllEntriesOfEmployee(@PathVariable("id") employeeId: Long): ResponseEntity<List<WorktimeEntryDto>> {
        return ResponseEntity.ok(worktimeEntryService.getAllEntriesOfEmployee(employeeId))
    }

    @GetMapping("$worktimeUrl/project/{id}")
    fun getAllEntriesOfProject(@PathVariable("id") projectId: Long): ResponseEntity<List<WorktimeEntryDto>> {
        return ResponseEntity.ok(worktimeEntryService.getAllEntriesOfProject(projectId))
    }

    @GetMapping("$worktimeUrl/project/hours/{id}")
    fun getHoursOnProject(@PathVariable("id") projectId: Long): ResponseEntity<Int> {
        return ok(worktimeEntryService.getHoursOnProject(projectId))
    }

    @GetMapping("$worktimeUrl/employee/{employeeId}")
    fun getAllEntriesOfEmployeeOnProject(@PathVariable("employeeId") employeeId: Long, @RequestParam(value = "projectId") projectId: Long): ResponseEntity<List<WorktimeEntryDto>> {
        return ResponseEntity.ok(
                worktimeEntryRepository.findAllByProjectProjectIdAndEmployeeEmployeeIdAndDeletedFalse(projectId, employeeId).map { worktimeEntryService.mapToDto(it) }
        )
    }

    @PostMapping(worktimeUrl)
    fun createEntry(@RequestBody worktimeEntryDto: WorktimeEntryDto): ResponseEntity<WorktimeEntryDto> {
        try {
            val entry = worktimeEntryService.createEntry(worktimeEntryDto)
            return ok(entry)
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong when creating a worktime entry", ex)
        }
    }

    @PutMapping(worktimeUrl)
    fun updateEntry(@RequestBody worktimeEntryDto: WorktimeEntryDto): ResponseEntity<WorktimeEntryDto> {
        try {
            val entry = worktimeEntryService.updateWorktimeEntry(worktimeEntryDto)
            return ok(entry)
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong when updating a worktime entry", ex)
        }
    }

    @DeleteMapping("$worktimeUrl/{id}")
    fun deleteWorkTimeEntry(@PathVariable("id") id: Long) {
        try {
            worktimeEntryService.deleteEntry(id)
        } catch (ex: Exception) {
            throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No Entry under the given id", ex)
        }
    }

}