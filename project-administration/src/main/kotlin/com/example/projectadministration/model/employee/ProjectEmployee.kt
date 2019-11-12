package com.example.projectadministration.model.employee

import com.example.projectadministration.model.AggregateState
import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class ProjectEmployee(
        @Id @GeneratedValue var id: Long?,
        val employeeId: Long,
        var firstname: String,
        var lastname: String,
        var mail: String,
        var department: String,
        var title: String,
        var deleted: Boolean,
        var state: AggregateState
): Serializable {
}