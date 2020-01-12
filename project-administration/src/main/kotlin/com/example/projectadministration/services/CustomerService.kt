package com.example.projectadministration.services

import com.example.projectadministration.model.aggregates.AggregateState
import com.example.projectadministration.model.aggregates.Customer
import com.example.projectadministration.model.dto.CustomerDto
import com.example.projectadministration.model.dto.CustomerSync
import com.example.projectadministration.repositories.CustomerRepository
import com.example.projectadministration.repositories.ProjectRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerService(
        val customerRepository: CustomerRepository,
        val projectRepository: ProjectRepository
): WorkflowPersistenceService<Customer>, SyncMappingService<Customer, CustomerSync>, MappingService<Customer, CustomerDto> {

    @Transactional
    override fun saveAggregateWithWorkflow(aggregate: Customer, compensationAggregate: Customer?): Customer {
        // Currently no synchronisation workflow exists. The method is implemented to ease further development
        aggregate.state = AggregateState.ACTIVE
        return customerRepository.save(aggregate)
    }

    override fun mapToSyncDto(aggregate: Customer): CustomerSync {
        return CustomerSync(
                aggregate.id!!,
                aggregate.customerName,
                aggregate.address,
                aggregate.contact,
                aggregate.deleted,
                aggregate.state
        )
    }

    override fun mapToDto(aggregate: Customer): CustomerDto {
        return CustomerDto(aggregate.id, aggregate.customerName, aggregate.address, aggregate.contact, aggregate.state)
    }

    override fun mapToEntity(dto: CustomerDto): Customer {
        return Customer(dto.id, dto.customerName, dto.address, dto.contact)
    }

    fun getAllCustomers(): List<CustomerDto> {
        return customerRepository.getAllByDeletedFalse().map { mapToDto(it) }
    }

    fun getCustomerById(id: Long): CustomerDto {
        return customerRepository.getByIdAndDeletedFalse(id).map { mapToDto(it) }.orElseThrow()
    }

    fun getCustomerOfProject(projectId: Long): CustomerDto {
        return projectRepository.getByIdAndDeletedFalse(projectId).map { mapToDto(it.customer) }.orElseThrow()
    }

    @Transactional
    fun createCustomer(customerDto: CustomerDto): CustomerDto {
        val customer = mapToEntity(customerDto)
        return mapToDto(saveAggregateWithWorkflow(customer, null))
    }

    @Transactional
    fun updateCustomer(customerDto: CustomerDto): CustomerDto {
        val customer = customerRepository.findById(customerDto.id!!).orElseThrow()
        if (customer.state == AggregateState.PENDING) throw RuntimeException("Customer is still pending")
        val compensationCustomer = customer.copy()
        if (customer.customerName != customerDto.customerName) {
            customer.changeName(customerDto.customerName)
        }
        if (customer.address != customerDto.address) {
            customer.moveCompanyLocation(customerDto.address)
        }
        if (customer.contact != customerDto.contact) {
            customer.changeCustomerContact(customerDto.contact)
        }
        return mapToDto(saveAggregateWithWorkflow(customer, compensationCustomer))
    }

    @Transactional
    fun deleteCustomer(id: Long) {
        if (projectRepository.getAllByCustomerIdAndDeletedFalse(id).isEmpty()) {
            val customer = customerRepository.getByIdAndDeletedFalse(id).orElseThrow {
                Exception("The customer you are trying to delete does not exist")
            }
            if (customer.state == AggregateState.PENDING) throw RuntimeException("Customer is still pending")
            customer.deleteAggregate()
            val compensationCustomer = customer.copy()
            saveAggregateWithWorkflow(customer, compensationCustomer)
        } else {
            throw Exception("The customer has projects assigned to it and cannot be deleted.")
        }
    }
}