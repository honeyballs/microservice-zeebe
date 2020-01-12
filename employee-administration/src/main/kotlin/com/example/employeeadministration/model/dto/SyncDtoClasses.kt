package com.example.employeeadministration.model.dto

import com.example.employeeadministration.model.aggregates.AggregateState
import com.example.employeeadministration.model.valueobjects.Address
import com.example.employeeadministration.model.valueobjects.BankDetails
import com.example.employeeadministration.model.valueobjects.CompanyMail
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal
import java.time.LocalDate

/**
 * DTO used for the replication of aggregates via Zeebe. This contains all fields but reduces relationships between aggregates to just the ids.
 */
data class DepartmentSync(val id: Long, val name: String, val deleted: Boolean, val state: AggregateState)

data class PositionSync(val id: Long, val title: String, val minHourlyWage: BigDecimal, val maxHourlyWage: BigDecimal, val deleted: Boolean, val state: AggregateState)

data class EmployeeSync(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val birthday: LocalDate,
        val address: Address,
        val bankDetails: BankDetails,
        val department: Long,
        val position: Long,
        val hourlyRate: BigDecimal,
        val companyMail: CompanyMail?,
        val availableVacationHours: Int,
        val deleted: Boolean,
        val state: AggregateState
)