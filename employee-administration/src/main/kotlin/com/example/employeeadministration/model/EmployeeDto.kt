package com.example.employeeadministration.model

import java.math.BigDecimal

data class EmployeeDto(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val address: String,
        val mail: String,
        val iban: String,
        val department: String,
        val title: String,
        val hourlyRate: BigDecimal,
        val state: AggregateState = AggregateState.PENDING
)