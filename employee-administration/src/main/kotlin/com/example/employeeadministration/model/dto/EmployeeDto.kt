package com.example.employeeadministration.model.dto

import com.example.employeeadministration.model.AggregateState
import java.math.BigDecimal

/**
 * DTO used for the REST APIs. This differs from the DTO used to synchronize Services via Zeebe,
 * for example the "deleted" field is not present here because deleted Aggregates would not be sent.
 */
data class EmployeeDto(
        var id: Long?,
        val firstname: String,
        val lastname: String,
        val address: String,
        val mail: String,
        val iban: String,
        val department: String,
        val title: String,
        val hourlyRate: BigDecimal,
        val state: AggregateState?
)