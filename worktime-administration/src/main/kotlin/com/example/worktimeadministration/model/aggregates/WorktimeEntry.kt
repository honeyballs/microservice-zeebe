package com.example.worktimeadministration.model.aggregates

import com.example.worktimeadministration.model.employee.Employee
import com.example.worktimeadministration.model.project.Project
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
        var pauseTimeInMinutes: Int = 0,
        @ManyToOne(cascade = [CascadeType.REFRESH])
        @JoinColumn(name="project_id")
        var project: Project,
        @ManyToOne(cascade = [CascadeType.REFRESH])
        @JoinColumn(name="employee_id")
        val employee: Employee,
        var description: String,
        var type: EntryType,
        deleted: Boolean = false,
        state: AggregateState = AggregateState.PENDING
) : Aggregate(id, state, deleted) {

    init {
        if (type == EntryType.WORK && !isPauseSufficient(calculateTimespan(startTime, endTime))) {
            throw Exception("Insufficient Pause time")
        } else if (type == EntryType.VACATION && !employeeHasEnoughVacationHours(calculateTimespan(startTime, endTime))) {
            throw Exception("Not enough vacation hours")
        }
        if (!timeFitsWithinProjectSpan(startTime) || !timeFitsWithinProjectSpan(endTime)) {
            throw Exception("Timeframe not within project dates")
        }
    }

    @Throws(Exception::class)
    fun adjustStartTime(newStartTime: LocalDateTime) {
        if (type == EntryType.WORK && !isPauseSufficient(calculateTimespan(newStartTime, endTime))) {
            throw Exception("Insufficient Pause time")
        } else if (type == EntryType.VACATION && !employeeHasEnoughVacationHours(calculateTimespan(newStartTime, endTime))) {
            throw Exception("Not enough vacation hours")
        }
        if (!timeFitsWithinProjectSpan(newStartTime) || !timeFitsWithinProjectSpan(endTime)) {
            throw Exception("Timeframe not within project dates")
        }
        startTime = newStartTime
    }

    @Throws(Exception::class)
    fun adjustEndTime(newEndTime: LocalDateTime) {
        if (type == EntryType.WORK && !isPauseSufficient(calculateTimespan(startTime, newEndTime))) {
            throw Exception("Insufficient Pause time")
        } else if (type == EntryType.VACATION && !employeeHasEnoughVacationHours(calculateTimespan(startTime, newEndTime))) {
            throw Exception("Not enough vacation hours")
        }
        if (!timeFitsWithinProjectSpan(startTime) || !timeFitsWithinProjectSpan(newEndTime)) {
            throw Exception("Timeframe not within project dates")
        }
        endTime = newEndTime
    }

    fun changeProject(newProject: Project) {
        this.project = project
        if (!timeFitsWithinProjectSpan(startTime) || !timeFitsWithinProjectSpan(endTime)) {
            throw Exception("Timeframe not within project dates")
        }
    }

    fun changeDescription(newDescription: String) {
        this.description = description
    }

    fun adjustPauseTime(pauseTimeInMinutes: Int) {
        if (isPauseSufficient(calculateTimespan(startTime, endTime), pauseTimeInMinutes)) {
            this.pauseTimeInMinutes = pauseTimeInMinutes
        } else {
            throw Exception("The pause time is not sufficient for the provided timeframe")
        }
    }

    fun isPauseSufficient(timespan: Int, pause: Int? = null): Boolean {
        return timespan < 8 || timespan in 8..9 && pause ?: pauseTimeInMinutes >= 30
                || timespan >= 10 && pause ?:pauseTimeInMinutes >= 60
    }

    fun timeFitsWithinProjectSpan(time: LocalDateTime): Boolean {
        val endDateToUse = project.endDate ?: project.projectedEndDate
        return time.toLocalDate().isAfter(project.startDate) && time.toLocalDate().isBefore(endDateToUse)
    }

    fun employeeHasEnoughVacationHours(timespan: Int): Boolean {
        return (employee.usedVacationHours + timespan) <= employee.availableVacationHours
    }

    fun calculateTimespan(startTime: LocalDateTime, endTime: LocalDateTime): Int {
        return startTime.until(endTime, ChronoUnit.HOURS).toInt()
    }

    override fun deleteAggregate() {
        this.deleted = true
    }

    override fun copy(): WorktimeEntry {
        return WorktimeEntry(
                this.id,
                this.startTime,
                this.endTime,
                this.pauseTimeInMinutes,
                this.project.copy(),
                this.employee.copy(),
                this.description,
                this.type,
                this.deleted,
                this.state
        )
    }

}

enum class EntryType(type: String) {
    WORK("WORK"),
    VACATION("VACATION"),
    SICK("SICK")
}