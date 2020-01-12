package com.example.worktimeadministration.model.aggregates

/**
 * Enum describing the possible states an aggregate can be in.
 * PENDING means the aggregate was changed and is not synchronized, ACTIVE means all replications are up to date.
 * A FAILED aggregate could not be replicated.
 */
enum class AggregateState(state: String) {
    PENDING("PENDING"), ACTIVE("ACTIVE"), FAILED("FAILED")
}