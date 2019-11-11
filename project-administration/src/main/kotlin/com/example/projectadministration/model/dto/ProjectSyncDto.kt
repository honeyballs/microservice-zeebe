package com.example.projectadministration.model.dto

import com.example.projectadministration.configuration.datePattern
import com.example.projectadministration.model.AggregateState
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

class ProjectSyncDto(
        val id: Long,
        val name: String,
        val customer: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) val startDate: LocalDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) var endDate: LocalDate?,
        val employees: MutableSet<Long>,
        val deleted: Boolean,
        val state: AggregateState
)