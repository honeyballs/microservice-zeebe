package com.example.projectadministration.model.employee.dto

import com.example.projectadministration.model.AggregateState
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmployeeSyncDto(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val mail: String,
        val department: String,
        val title: String,
        val deleted: Boolean,
        val state: AggregateState
) {
}