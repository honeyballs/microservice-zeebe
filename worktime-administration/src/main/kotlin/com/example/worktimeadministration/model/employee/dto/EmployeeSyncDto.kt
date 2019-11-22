package com.example.worktimeadministration.model.employee.dto

import com.example.worktimeadministration.model.AggregateState
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * DTO used for the replication of aggregates via Zeebe.
 * This DTO contains only fields relevant for this service, all other fields will be ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EmployeeSyncDto(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val deleted: Boolean,
        val state: AggregateState
) {
}