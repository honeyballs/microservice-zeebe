package com.example.worktimeadministration.model.project

import com.example.worktimeadministration.model.aggregates.AggregateState
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDate


@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectSync(
        val id: Long,
        val name: String,
        val description: String,
        val startDate: LocalDate,
        val projectedEndDate: LocalDate,
        val endDate: LocalDate?,
        val employees: MutableSet<Long>,
        val deleted: Boolean,
        val state: AggregateState
)