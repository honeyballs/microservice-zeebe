package com.example.projectadministration.model.aggregates

import javax.persistence.*

/**
 * Aggregate super class containing common aggregate fields and functions.
 */
@MappedSuperclass
@SequenceGenerator(name = "project_seq", sequenceName = "project_id_sequence")
abstract class Aggregate(@Id @GeneratedValue(strategy = GenerationType.AUTO, generator = "project_seq") var id: Long?, var state: AggregateState = AggregateState.PENDING, var deleted: Boolean = false) {

    fun changeAggregateState(state: AggregateState) {
        this.state = state
    }

    abstract fun deleteAggregate()

    // The copy function returns a separate instance of the aggregate.
    // Used to retain a version before changes are applied.
    abstract fun copy(): Aggregate
}