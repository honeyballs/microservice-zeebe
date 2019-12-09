package com.example.worktimeadministration.services

/**
 * Define methods to map aggregates to their corresponding synchronization dto's.
 */
interface SyncMappingService<in Aggregate, out SyncDto> {

    fun mapToSyncDto(aggregate: Aggregate): SyncDto

}