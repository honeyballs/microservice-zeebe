package com.example.worktimeadministration.model.employee.dto

import com.example.worktimeadministration.model.AggregateState
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmployeeSyncDto(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val deleted: Boolean,
        val state: AggregateState
) {
}