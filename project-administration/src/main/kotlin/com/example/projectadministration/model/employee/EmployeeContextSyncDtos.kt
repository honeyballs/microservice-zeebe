package com.example.projectadministration.model.employee

import com.example.projectadministration.model.aggregates.AggregateState
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName


@JsonIgnoreProperties(ignoreUnknown = true)
data class DepartmentSync(val id: Long, val name: String, val deleted: Boolean, val state: AggregateState)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PositionSync(val id: Long, val title: String, val deleted: Boolean, val state: AggregateState)

@JsonIgnoreProperties(ignoreUnknown = true)
class EmployeeSync(
        val id: Long,
        val firstname: String,
        val lastname: String,
        val department: Long,
        val position: Long,
        val deleted: Boolean,
        val state: AggregateState
) {

    lateinit var companyMail: String

    // Since mail is stored in a value object which isn't relevant for this service it is reduced to a string
    @JsonProperty("companyMail")
    fun unpackMail(mail: Map<String, Any>) {
        this.companyMail = mail["mail"] as String
    }

}