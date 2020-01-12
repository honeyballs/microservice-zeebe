package com.example.employeeadministration.jobhandlers

import com.example.employeeadministration.model.aggregates.AggregateState
import com.example.employeeadministration.model.dto.DepartmentSync
import com.example.employeeadministration.model.dto.PositionSync
import com.example.employeeadministration.repositories.DepartmentRepository
import com.example.employeeadministration.repositories.PositionRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.zeebe.client.api.response.ActivatedJob
import io.zeebe.client.api.worker.JobClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.UnexpectedRollbackException
import org.springframework.transaction.annotation.Transactional

@Component
class PositionJobHandlers {

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var positionSyncTransactionalService: PositionSyncTransactionalService

    val activatePosition: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING ACTIVATION JOB")
        val positionId = mapper.readValue<PositionSync>(job.variablesAsMap["position"] as String).id
        positionSyncTransactionalService.handleActivation(positionId)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensatePosition: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING COMPENSATION JOB")
        val variables = job.variablesAsMap
        val positionId = mapper.readValue<PositionSync>(job.variablesAsMap["position"] as String).id
        if (variables.containsKey("compensationPosition")) {
            positionSyncTransactionalService.handleCompensation(positionId, mapper.readValue<PositionSync>(variables["compensationPosition"] as String))
        } else {
            positionSyncTransactionalService.handleCompensation(positionId, null)
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

}

@Service
class PositionSyncTransactionalService(
        val positionRepository: PositionRepository
): SyncService<PositionSync> {

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleActivation(positionId: Long) {
        val position = positionRepository.findById(positionId).orElseThrow()
        position.state = AggregateState.ACTIVE
        positionRepository.save(position)
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleCompensation(positionId: Long, compensationPosition: PositionSync?) {
        if (compensationPosition != null) {
            positionRepository.findById(positionId).ifPresent {
                it.title = compensationPosition.title
                it.minHourlyWage = compensationPosition.minHourlyWage
                it.maxHourlyWage = compensationPosition.maxHourlyWage
                it.deleted = compensationPosition.deleted
                it.state = AggregateState.ACTIVE
                positionRepository.save(it)
            }
        } else {
            positionRepository.findById(positionId).ifPresent {
                positionRepository.deleteById(positionId)
            }
        }
    }

}