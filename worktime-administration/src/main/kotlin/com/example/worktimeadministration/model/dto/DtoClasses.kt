package com.example.worktimeadministration.model.dto

import com.example.worktimeadministration.configuration.datePattern
import com.example.worktimeadministration.configuration.dateTimePattern
import com.example.worktimeadministration.model.aggregates.AggregateState
import com.example.worktimeadministration.model.aggregates.EntryType
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

@JsonIgnoreProperties(value = ["state"], allowGetters = true)
class EmployeeDto(
        val id: Long,
        val firstname: String,
        val lastname: String,
        @JsonProperty("state") val state: AggregateState?
)

@JsonIgnoreProperties(value = ["state"], allowGetters = true)
class ProjectDto(
        val id: Long,
        val name: String,
        val description: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) val startDate: LocalDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) val projectedEndDate: LocalDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) val endDate: LocalDate?,
        val employees: Set<Long>,
        @JsonProperty("state") val state: AggregateState?
)

@JsonIgnoreProperties(value = ["state"], allowGetters = true)
class WorktimeEntryDto(
        val id: Long?,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimePattern) var startTime: LocalDateTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimePattern) var endTime: LocalDateTime,
        var pauseTimeInMinutes: Int = 0,
        val project: ProjectDto,
        val employee: EmployeeDto,
        var description: String,
        var type: EntryType,
        @JsonProperty("state") val state: AggregateState?
)