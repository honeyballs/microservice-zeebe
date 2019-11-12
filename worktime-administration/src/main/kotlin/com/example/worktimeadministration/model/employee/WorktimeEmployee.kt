package com.example.worktimeadministration.model.employee

import com.example.worktimeadministration.model.AggregateState
import com.example.worktimeadministration.model.project.WorktimeProject
import java.io.Serializable
import javax.persistence.*

@Entity
data class WorktimeEmployee(
        @Id @GeneratedValue var id: Long?,
        val employeeId: Long,
        var firstname: String,
        var lastname: String,
        var deleted: Boolean,
        var state: AggregateState
): Serializable {
}