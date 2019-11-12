package com.example.worktimeadministration.model.project

import com.example.worktimeadministration.configuration.datePattern
import com.example.worktimeadministration.model.AggregateState
import com.example.worktimeadministration.model.employee.WorktimeEmployee
import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

@Entity
data class WorktimeProject(
        @Id @GeneratedValue var id: Long?,
        val projectId: Long,
        val name: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) val startDate: LocalDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = datePattern) var endDate: LocalDate?,
        var deleted: Boolean,
        var state: AggregateState,
        @ManyToMany(cascade = [CascadeType.REFRESH], fetch = FetchType.EAGER)
        @JoinTable(name="project_employees",
                joinColumns = [JoinColumn(name= "project_id", referencedColumnName = "projectId")],
                inverseJoinColumns = [JoinColumn(name = "employee_id", referencedColumnName = "employeeId")])
        var employees: MutableSet<WorktimeEmployee> = mutableSetOf()
): Serializable {
}