package com.example.projectadministration.model.dto

import com.example.projectadministration.configuration.datePattern
import com.example.projectadministration.model.AggregateState
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * DTOs used for the REST APIs. These differ from DTOs used to synchronize Services via Zeebe,
 * for example the "deleted" field is not present here because deleted Aggregates would not be sent.
 */

data class ProjectDto(
        var id: Long?,
        val name: String,
        val customer: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) val startDate: LocalDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) var endDate: LocalDate?,
        val employees: MutableSet<ProjectEmployeeDto>,
        val state: AggregateState?
)

data class ProjectEmployeeDto(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val mail: String,
        val department: String,
        val title: String
)