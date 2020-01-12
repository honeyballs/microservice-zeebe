package com.example.projectadministration.model.aggregates

import com.example.projectadministration.model.employee.Employee
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

/**
 * The Project aggregate.
 */
@Entity
class Project(
        id: Long?,
        var name: String,
        var description: String,
        val startDate: LocalDate,
        var projectedEndDate: LocalDate,
        var endDate: LocalDate?,
        @ManyToMany(cascade = [CascadeType.REFRESH])
        @JoinTable(name = "project_employees",
                joinColumns = [JoinColumn(name = "project_id")],
                inverseJoinColumns = [JoinColumn(name = "employee_id")])
        var employees: MutableSet<Employee>,
        @ManyToOne(cascade = [CascadeType.REFRESH]) @JoinColumn(name = "fk_customer") val customer: Customer,
        deleted: Boolean = false,
        state: AggregateState = AggregateState.PENDING
): Aggregate(id, state, deleted), Serializable {

    fun delayProject(newProjectedDate: LocalDate) {
        this.projectedEndDate = newProjectedDate
    }

    fun finishProject(endDate: LocalDate) {
        this.endDate = endDate
    }

    fun updateProjectDescription(description: String) {
        this.description = description
    }

    fun addEmployeeToProject(employee: Employee) {
        this.employees.add(employee)
    }

    fun removeEmployeeFromProject(employee: Employee) {
        this.employees.remove(employee)
    }

    fun changeEmployeesWorkingOnProject(employees: MutableSet<Employee>) {
        this.employees = employees
    }

    override fun deleteAggregate() {
        this.deleted = true
    }

    override fun copy(): Project {
        return Project(
                this.id!!,
                this.name,
                this.description,
                this.startDate,
                this.projectedEndDate,
                this.endDate,
                this.employees.map { it.copy() }.toMutableSet(),
                this.customer.copy(),
                this.deleted,
                this.state
        )
    }
}