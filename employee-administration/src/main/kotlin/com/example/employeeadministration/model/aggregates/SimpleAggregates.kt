package com.example.employeeadministration.model.aggregates

import java.math.BigDecimal
import java.math.RoundingMode
import javax.persistence.*

const val DEPARTMENT_AGGREGATE_NAME = "department"

/**
 * Department Aggregate
 */
@Entity
class Department(
        id: Long?,
        @Column(name = "department_name") var name: String,
        deleted: Boolean = false,
        state: AggregateState = AggregateState.PENDING
) : Aggregate(id, state, deleted) {

    fun renameDepartment(name: String) {
        this.name = name
    }

    override fun deleteAggregate() {
        deleted = true
    }

    override fun copy(): Department {
        return Department(this.id, this.name, this.deleted, this.state)
    }


}


const val POSITION_AGGREGATE_NAME = "position"

/**
 * Position aggregate
 */
@Entity
class Position(
        id: Long?,
        var title: String,
        minHourlyWage: BigDecimal,
        maxHourlyWage: BigDecimal,
        deleted: Boolean = false,
        state: AggregateState = AggregateState.PENDING
) : Aggregate(id, state, deleted) {

    var minHourlyWage: BigDecimal = minHourlyWage.setScale(2, RoundingMode.HALF_UP)
        set(value) {
            field = value.setScale(2, RoundingMode.HALF_UP)
        }

    var maxHourlyWage: BigDecimal = maxHourlyWage.setScale(2, RoundingMode.HALF_UP)
        set(value) {
            field = value.setScale(2, RoundingMode.HALF_UP)
        }

    fun changePositionTitle(title: String) {
        this.title = title
    }

    fun adjustWageRange(min: BigDecimal?, max: BigDecimal?) {
        minHourlyWage = min ?: minHourlyWage
        maxHourlyWage = max ?: maxHourlyWage
    }

    override fun deleteAggregate() {
        deleted = true
    }

    override fun copy(): Position {
        return Position(this.id, this.title, this.minHourlyWage, this.maxHourlyWage, this.deleted, this.state)
    }


}

/**
 * Function to check whether a rate is within the limits of a job position
 */
fun Position.isRateInRange(rateToCheck: BigDecimal): Boolean {
    return rateToCheck in this.minHourlyWage..this.maxHourlyWage
}

