package com.example.projectadministration.model

import com.example.projectadministration.model.employee.ProjectEmployee
import java.time.LocalDate
import javax.persistence.*

@Entity
class Project(
        id: Long?,
        val name: String,
        val customer: String,
        val startDate: LocalDate,
        var endDate: LocalDate?,
        @ManyToMany(cascade = [CascadeType.REFRESH])
        @JoinTable(name="project_employees",
                joinColumns = [JoinColumn(name= "project_id")],
                inverseJoinColumns = [JoinColumn(name = "employee_id", referencedColumnName = "employeeId")])
        val employees: MutableSet<ProjectEmployee>,
        deleted: Boolean = false,
        state: AggregateState = AggregateState.PENDING
): Aggregate(id, state, deleted) {


    fun finishProject(date: LocalDate) {
        if (this.endDate == null) {
            this.endDate = date
        }
    }

    fun addEmployeeToProject(employee: ProjectEmployee) {
        this.employees.add(employee)
    }

    fun removeEmployeeFromProject(employee: ProjectEmployee) {
        this.employees.remove(employee)
    }

    override fun deleteAggregate() {
        this.deleted = true
    }


}