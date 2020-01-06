package com.example.worktimeadministration.model.employee

import com.example.worktimeadministration.model.AggregateState
import com.example.worktimeadministration.model.project.WorktimeProject
import java.io.Serializable
import javax.persistence.*

/**
 * The Worktime Service's representation of an employee.
 */
@Entity
data class WorktimeEmployee(
        @Id @GeneratedValue(strategy = GenerationType.AUTO, generator = "worktime_seq") var id: Long?,
        val employeeId: Long,
        var firstname: String,
        var lastname: String,
        var deleted: Boolean,
        var state: AggregateState
): Serializable {
}