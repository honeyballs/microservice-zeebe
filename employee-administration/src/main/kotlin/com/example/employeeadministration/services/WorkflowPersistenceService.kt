package com.example.employeeadministration.services

import com.example.employeeadministration.model.Aggregate

/**
 * Define a persistence method to save an aggregate and create a corresponding workflow.
 */
interface WorkflowPersistenceService<Aggregate> {

    fun saveAggregateWithWorkflow(aggregate: Aggregate, compensationAggregate: Aggregate?): Aggregate

}