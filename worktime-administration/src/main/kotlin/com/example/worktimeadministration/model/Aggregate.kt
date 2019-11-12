package com.example.worktimeadministration.model

import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class Aggregate(@Id @GeneratedValue var id: Long?, var state: AggregateState = AggregateState.PENDING, var deleted: Boolean = false) {

    fun changeAggregateState(state: AggregateState) {
        this.state = state
    }

    abstract fun deleteAggregate()
}