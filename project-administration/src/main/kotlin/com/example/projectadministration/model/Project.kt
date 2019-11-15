package com.example.projectadministration.model

import com.example.projectadministration.model.employee.ProjectEmployee
import java.io.Serializable
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
        val employees: MutableSet<ProjectEmployee> = mutableSetOf(),
        deleted: Boolean = false,
        state: AggregateState = AggregateState.PENDING
): Aggregate(id, state, deleted), Serializable {


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

    override fun copy(): Project {
        return Project(
                this.id!!,
                this.name,
                this.customer,
                this.startDate,
                this.endDate,
                this.employees.map{ it.copy() }.toMutableSet(),
                this.deleted,
                this.state
        )
    }
}