package com.example.worktimeadministration.services

/**
 * Define methods to map aggregates to their corresponding dto's.
 */
interface MappingService<Aggregate, Dto> {

    fun mapToDto(aggregate: Aggregate): Dto
    fun mapToEntity(dto: Dto): Aggregate

}