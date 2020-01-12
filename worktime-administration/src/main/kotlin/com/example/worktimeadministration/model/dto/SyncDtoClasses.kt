package com.example.worktimeadministration.model.dto

import com.example.worktimeadministration.model.aggregates.AggregateState
import com.example.worktimeadministration.model.aggregates.EntryType
import java.time.LocalDateTime

class WorktimeEntrySync(
        val id: Long,
        var startTime: LocalDateTime,
        var endTime: LocalDateTime,
        var pauseTimeInMinutes: Int = 0,
        val project: Long,
        val employee: Long,
        var description: String,
        var type: EntryType,
        var deleted: Boolean = false,
        var state: AggregateState
)