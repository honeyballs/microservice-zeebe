package com.example.employeeadministration.repositories

import com.example.employeeadministration.model.aggregates.Position
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PositionRepository : JpaRepository<Position, Long> {

    fun getAllByDeletedFalse(): List<Position>
    fun getByIdAndDeletedFalse(id: Long): Optional<Position>
    fun getByTitleAndDeletedFalse(title: String): Optional<Position>

}