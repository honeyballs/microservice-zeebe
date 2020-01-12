package com.example.projectadministration.model.employee

import com.example.projectadministration.model.aggregates.AggregateState
import javax.persistence.*

@Entity
data class Department(@Id @GeneratedValue(strategy = GenerationType.AUTO, generator = "project_seq") var dbId: Long?, val departmentId: Long, var name: String, var deleted: Boolean = false, var state: AggregateState)

@Entity
data class Position(@Id @GeneratedValue(strategy = GenerationType.AUTO, generator = "project_seq") var dbId: Long?, val positionId: Long, var title: String, var deleted: Boolean = false, var state: AggregateState)

@Entity
data class Employee(
        @Id @GeneratedValue(strategy = GenerationType.AUTO, generator = "project_seq") var dbId: Long?,
        val employeeId: Long,
        var firstname: String,
        var lastname: String,
        @ManyToOne @JoinColumn(name = "fk_department") var department: Department,
        @ManyToOne @JoinColumn(name = "fk_position") var position: Position,
        var companyMail: String,
        var deleted: Boolean = false,
        var state: AggregateState
)