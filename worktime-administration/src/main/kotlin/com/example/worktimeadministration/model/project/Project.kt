package com.example.worktimeadministration.model.project

import com.example.worktimeadministration.model.aggregates.AggregateState
import com.example.worktimeadministration.model.employee.Employee
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

@Entity
data class Project(
        @Id @GeneratedValue(strategy = GenerationType.AUTO, generator = "worktime_seq") var dbId: Long?,
        val projectId: Long,
        var name: String,
        var description: String,
        var startDate: LocalDate,
        var projectedEndDate: LocalDate,
        var endDate: LocalDate?,
        @ManyToMany(cascade = [CascadeType.REFRESH], fetch = FetchType.LAZY)
        @JoinTable(name = "project_employees",
                joinColumns = [JoinColumn(name = "project_id", referencedColumnName = "projectId")],
                inverseJoinColumns = [JoinColumn(name = "employee_id", referencedColumnName = "employeeId")])
        var employees: MutableSet<Employee>,
        var deleted: Boolean,
        var state: AggregateState
): Serializable {
}