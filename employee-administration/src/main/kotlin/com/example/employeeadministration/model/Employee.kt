package com.example.employeeadministration.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal
import javax.persistence.Entity

@JsonTypeName("Employee")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@Entity
class Employee(
        id: Long?,
        var firstname: String,
        var lastname: String,
        var address: String,
        var mail: String,
        var iban: String,
        var department: String,
        var title: String,
        var hourlyRate: BigDecimal,
        deleted: Boolean = false,
        state: AggregateState = AggregateState.PENDING
): Aggregate(id, state, deleted) {

    fun changesName(firstname: String, lastname: String) {
        this.firstname = firstname
        this.lastname = lastname
    }

    fun moves(address: String) {
        this.address = address
    }

    fun adjustRate(rate: BigDecimal) {
        this.hourlyRate = rate
    }

    fun movesToDepartment(department: String) {
        this.department = department
    }

    override fun deleteAggregate() {
        this.deleted = true
    }

}