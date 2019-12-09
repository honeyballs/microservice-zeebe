package com.example.worktimeadministration.services

/**
 * Define methods to map aggregates to their corresponding dto's.
 */
interface MappingService<in Aggregate, out Dto> {

    fun mapToDto(aggregate: Aggregate): Dto

}