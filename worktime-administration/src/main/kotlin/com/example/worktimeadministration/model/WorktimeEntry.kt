package com.example.worktimeadministration.model

import com.example.worktimeadministration.model.employee.WorktimeEmployee
import com.example.worktimeadministration.model.project.WorktimeProject
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.Exception

/**
 * The WorktimeEntry aggregate.
 */
@Entity
class WorktimeEntry(
        id: Long?,
        var startTime: LocalDateTime,
        var endTime: LocalDateTime,
        var pauseTimeInMinutes: Int,
        @ManyToOne(cascade = [CascadeType.REFRESH])
        @JoinColumn(name = "projectId")
        var project: WorktimeProject,
        @ManyToOne(cascade = [CascadeType.REFRESH])
        @JoinColumn(name = "employeeId")
        val employee: WorktimeEmployee,
        var description: String,
        deleted: Boolean = false,
        state: AggregateState = AggregateState.PENDING
) : Aggregate(id, state, deleted) {

    init {
        if (!isPauseSufficient(startTime, endTime, pauseTimeInMinutes)) {
            throwPauseException()
        }
        if (!isEntryInProjectSpan(project, startTime, endTime)) {
            throwRangeException()
        }
    }

    fun adjustStartTime(newStartTime: LocalDateTime) {
        if (isPauseSufficient(newStartTime, endTime, pauseTimeInMinutes)) {
            if (isEntryInProjectSpan(project, newStartTime, endTime)) {
                this.startTime = newStartTime
            } else {
                throwRangeException()
            }
        } else {
            throwPauseException()
        }
    }

    fun adjustEndTime(newEndTime: LocalDateTime) {
        if (isPauseSufficient(startTime, newEndTime, pauseTimeInMinutes)) {
            if (isEntryInProjectSpan(project, startTime, newEndTime)) {
                this.endTime = newEndTime
            } else {
                throwRangeException()
            }
        } else {
            throwPauseException()
        }
    }

    fun adjustPause(newPause: Int) {
        if (isPauseSufficient(startTime, endTime, newPause)) {
            this.pauseTimeInMinutes = newPause
        } else {
            throwPauseException()
        }
    }

    fun changeProject(project: WorktimeProject) {
        if (isEntryInProjectSpan(project, startTime, endTime)) {
            this.project = project
        } else {
            throwRangeException()
        }
    }

    fun changeDescription(description: String) {
        this.description = description
    }

    private fun isPauseSufficient(startTime: LocalDateTime, endTime: LocalDateTime, pause: Int): Boolean {
        val timespan = startTime.until(endTime, ChronoUnit.HOURS).toInt()
        return timespan < 8 || timespan in 8..9 && pause >= 30
                || timespan >= 10 && pause >= 60
    }

    private fun isEntryInProjectSpan(project: WorktimeProject, startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        if (project.endDate == null) {
            return startTime.toLocalDate().isAfter(project.startDate)
        }
        return startTime.toLocalDate().isAfter(project.startDate) && endTime.toLocalDate().isBefore(project.endDate)
    }

    @Throws(Exception::class)
    fun throwPauseException() {
        throw Exception("Insufficient Pause time")
    }

    @Throws(Exception::class)
    fun throwRangeException() {
        throw Exception("Entry not in project time range")
    }

    override fun deleteAggregate() {
        this.deleted = true
    }

    override fun copy(): WorktimeEntry {
        return WorktimeEntry(
                this.id!!,
                this.startTime,
                this.endTime,
                this.pauseTimeInMinutes,
                this.project.copy(),
                this.employee.copy(),
                this.description,
                this.deleted,
                this.state
        )
    }

}