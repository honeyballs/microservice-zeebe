package com.example.worktimeadministration.controllers

import com.example.worktimeadministration.model.dto.WorktimeEmployeeDto
import com.example.worktimeadministration.model.dto.WorktimeEntryDto
import com.example.worktimeadministration.services.WorktimeEntryService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*

const val url = "worktime"

@RestController
class WorktimeEntryController(val worktimeEntryService: WorktimeEntryService) {

    @GetMapping(url)
    fun getAllEntries(): ResponseEntity<List<WorktimeEntryDto>> {
        return ok(worktimeEntryService.getAll())
    }

    @GetMapping("$url/{id}")
    fun getEntryById(@PathVariable("id") id: Long): ResponseEntity<WorktimeEntryDto> {
        return ok(worktimeEntryService.getById(id))
    }

    @GetMapping("$url/employee/{id}")
    fun getEntriesOfEmployee(@PathVariable("id") id: Long): ResponseEntity<List<WorktimeEntryDto>> {
        return ok(worktimeEntryService.getByEmployee(id))
    }

    @GetMapping("$url/project/{id}")
    fun getEntriesOfProject(@PathVariable("id") id: Long): ResponseEntity<List<WorktimeEntryDto>> {
        return ok(worktimeEntryService.getByProject(id))
    }

    @PostMapping(url)
    fun createEntry(@RequestBody worktimeEntryDto: WorktimeEntryDto): ResponseEntity<WorktimeEntryDto> {
        return ok(worktimeEntryService.createWorktimeEntry(worktimeEntryDto))
    }

    @PutMapping(url)
    fun updateEntry(@RequestBody worktimeEntryDto: WorktimeEntryDto): ResponseEntity<WorktimeEntryDto> {
        return ok(worktimeEntryService.updateWorktimeEntry(worktimeEntryDto))
    }

    @DeleteMapping("$url/{id}")
    fun deleteEntry(@PathVariable("id") id: Long): ResponseEntity<WorktimeEntryDto> {
        return ok(worktimeEntryService.deleteWorktimeEntry(id))
    }

}