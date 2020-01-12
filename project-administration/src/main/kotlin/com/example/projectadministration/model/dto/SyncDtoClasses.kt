package com.example.projectadministration.model.dto

import com.example.projectadministration.configuration.datePattern
import com.example.projectadministration.model.aggregates.Address
import com.example.projectadministration.model.aggregates.AggregateState
import com.example.projectadministration.model.aggregates.CustomerContact
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDate

/**
 * DTO used for the replication of aggregates via Zeebe. This contains all fields but reduces relationships between aggregates to just the ids.
 */

data class ProjectSync(
        val id: Long,
        val name: String,
        val description: String,
        val startDate: LocalDate,
        val projectedEndDate: LocalDate,
        val endDate: LocalDate?,
        val employees: MutableSet<Long>,
        val customer: Long,
        val deleted: Boolean,
        val state: AggregateState
)

data class CustomerSync(val id: Long, val customerName: String, val address: Address, val contact: CustomerContact, val deleted: Boolean, val state: AggregateState)