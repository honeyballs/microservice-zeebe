package com.example.worktimeadministration.model

import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

/**
 * Aggregate super class containing common aggregate fields and functions.
 */
@MappedSuperclass
abstract class Aggregate(@Id @GeneratedValue var id: Long?, var state: AggregateState = AggregateState.PENDING, var deleted: Boolean = false) {

    fun changeAggregateState(state: AggregateState) {
        this.state = state
    }

    abstract fun deleteAggregate()

    // The copy function returns a separate instance of the aggregate.
    // Used to retain a version before changes are applied.
    abstract fun copy(): Aggregate
}