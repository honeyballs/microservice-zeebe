package com.example.worktimeadministration.model.employee

import com.example.worktimeadministration.model.aggregates.AggregateState
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class EmployeeSync(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val deleted: Boolean,
        val availableVacationHours: Int,
        val state: AggregateState
) {

    lateinit var companyMail: String

    // Since mail is stored in a value object which isn't relevant for this service it is reduced to a string
    @JsonProperty("companyMail")
    fun unpackMail(mail: Map<String, Any>) {
        this.companyMail = mail["mail"] as String
    }

}