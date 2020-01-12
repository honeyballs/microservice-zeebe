package com.example.employeeadministration.services

/**
 * Define methods to map aggregates to their corresponding dto's.
 */
interface MappingService<Aggregate, Dto> {

    fun mapToDto(aggregate: Aggregate): Dto
    fun mapToEntity(dto: Dto): Aggregate

}