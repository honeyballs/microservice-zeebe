package com.example.worktimeadministration.jobhandlers

/**
 * This interface defines service methods to handle aggregate changes.
 */
interface SyncService<in SyncDto> {

    fun handleSynchronization(dto: SyncDto) {}
    fun handleActivation(id: Long)
    fun handleCompensation(id: Long, dto: SyncDto?)

}