package com.example.worktimeadministration.model.dto

import com.example.worktimeadministration.configuration.datePattern
import com.example.worktimeadministration.configuration.dateTimePattern
import com.example.worktimeadministration.model.AggregateState
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTOs used for the REST APIs. Does not contain a deleted field since deleted entries would not be sent.
 */
data class WorktimeEmployeeDto(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val projects: MutableSet<WorktimeProjectDto>?,
        val state: AggregateState?
)

data class WorktimeProjectDto(
        val id: Long,
        val name: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) val startDate: LocalDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) var endDate: LocalDate?,
        val state: AggregateState?
)

data class WorktimeEntryDto(
        var id: Long?,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimePattern) val startTime: LocalDateTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimePattern) val endTime: LocalDateTime,
        val pauseTimeInMinutes: Int,
        val project: WorktimeProjectDto,
        val employee: WorktimeEmployeeDto,
        val description: String,
        val state: AggregateState?
)