package com.example.employeeadministration.model

enum class AggregateState(state: String) {
    PENDING("PENDING"), ACTIVE("ACTIVE"), FAILED("FAILED")
}