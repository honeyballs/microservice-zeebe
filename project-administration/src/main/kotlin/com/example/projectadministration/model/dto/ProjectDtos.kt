package com.example.projectadministration.model.dto

import com.example.projectadministration.model.AggregateState
import java.time.LocalDate

data class ProjectDto(
        val id: Long,
        val name: String,
        val customer: String,
        val startDate: LocalDate,
        var endDate: LocalDate?,
        val employees: MutableSet<ProjectEmployeeDto>,
        val state: AggregateState
)

data class ProjectEmployeeDto(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val mail: String,
        val department: String,
        val title: String
)