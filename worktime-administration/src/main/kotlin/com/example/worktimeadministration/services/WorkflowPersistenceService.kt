package com.example.worktimeadministration.services

import org.springframework.stereotype.Service

/**
 * Define a persistence method to save an aggregate and create a corresponding workflow.
 */
interface WorkflowPersistenceService<Aggregate> {

    fun saveAggregateWithWorkflow(aggregate: Aggregate, compensationAggregate: Aggregate?): Aggregate

}