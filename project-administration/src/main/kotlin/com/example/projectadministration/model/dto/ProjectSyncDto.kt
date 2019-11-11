package com.example.projectadministration.model.dto

import com.example.projectadministration.model.AggregateState
import java.time.LocalDate

class ProjectSyncDto(
        val id: Long,
        val name: String,
        val customer: String,
        val startDate: LocalDate,
        var endDate: LocalDate?,
        val employees: MutableSet<Long>,
        val deleted: Boolean,
        val state: AggregateState
)