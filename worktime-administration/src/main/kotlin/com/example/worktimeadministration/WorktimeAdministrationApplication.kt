package com.example.worktimeadministration

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WorktimeAdministrationApplication

fun main(args: Array<String>) {
    runApplication<WorktimeAdministrationApplication>(*args)
}
