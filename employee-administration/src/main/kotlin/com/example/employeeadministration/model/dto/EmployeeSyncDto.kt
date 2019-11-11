package com.example.employeeadministration.model.dto

import com.example.employeeadministration.model.AggregateState
import java.math.BigDecimal

data class EmployeeSyncDto(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val address: String,
        val mail: String,
        val iban: String,
        val department: String,
        val title: String,
        val hourlyRate: BigDecimal,
        val deleted: Boolean = false,
        val state: AggregateState = AggregateState.PENDING
)