package com.example.projectadministration.repositories

import com.example.projectadministration.model.aggregates.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CustomerRepository: JpaRepository<Customer, Long> {

    fun getAllByDeletedFalse(): List<Customer>
    fun getByIdAndDeletedFalse(id: Long): Optional<Customer>

}