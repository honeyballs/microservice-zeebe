package com.example.worktimeadministration.model.project.dto

import com.example.worktimeadministration.configuration.datePattern
import com.example.worktimeadministration.model.AggregateState
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * DTO used for the replication of aggregates via Zeebe.
 * This DTO contains only fields relevant for this service, all other fields will be ignored.
 */
class ProjectSyncDto(
        val id: Long,
        val name: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) val startDate: LocalDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) var endDate: LocalDate?,
        val employees: MutableSet<Long>,
        val deleted: Boolean,
        val state: AggregateState
)