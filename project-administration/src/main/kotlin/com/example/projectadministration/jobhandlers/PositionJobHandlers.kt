package com.example.projectadministration.jobhandlers

import com.example.projectadministration.model.aggregates.AggregateState
import com.example.projectadministration.model.employee.Department
import com.example.projectadministration.model.employee.DepartmentSync
import com.example.projectadministration.model.employee.Position
import com.example.projectadministration.model.employee.PositionSync
import com.example.projectadministration.repositories.employee.DepartmentRepository
import com.example.projectadministration.repositories.employee.PositionRepository
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
class PositionJobHandlers(
        val positionSyncTransactionService: PositionSyncTransactionService,
        val objectMapper: ObjectMapper
) {

    val synchronizePosition: (JobClient, ActivatedJob) -> Unit = { jobClient, job ->
        println("EXECUTING POSITION SYNC JOB")
        val positionDto = objectMapper.readValue<PositionSync>(job.variablesAsMap["position"] as String)
        try {
            positionSyncTransactionService.handleSynchronization(positionDto)
            // If no exception was thrown the synchronization was successful.
            // A String Boolean map is used to define new process variables returned to Zeebe by this job.
            val result = mapOf("projectPositionSynced" to true)
            // Using the provided JobClient we tell Zeebe that the job is complete and pass our map as new process variables
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        } catch (e: Exception) {
            // If an exception was thrown the job is also complete, but set the map value to false.
            val result = mapOf("projectPositionSynced" to false)
            jobClient.newCompleteCommand(job.key)
                    .variables(result)
                    .send()
                    .join()
        }
    }

    val activatePosition: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING POSITION ACTIVATION JOB")
        val positionId = objectMapper.readValue<PositionSync>(job.variablesAsMap["position"] as String).id
        positionSyncTransactionService.handleActivation(positionId)
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

    val compensatePosition: (JobClient, ActivatedJob) -> Unit = { jobClient: JobClient, job: ActivatedJob ->
        println("EXECUTING POSITION COMPENSATION JOB")
        val variables = job.variablesAsMap
        val positionId = objectMapper.readValue<PositionSync>(variables["position"] as String).id
        if (variables.containsKey("compensationPosition")) {
            positionSyncTransactionService.handleCompensation(positionId, objectMapper.readValue<PositionSync>(variables["compensationPosition"] as String))
        } else {
            positionSyncTransactionService.handleCompensation(positionId, null)
        }
        jobClient.newCompleteCommand(job.key)
                .send()
                .join()
    }

}

@Service
class PositionSyncTransactionService(
    val positionRepository: PositionRepository
): SyncService<PositionSync> {

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleSynchronization(positionDto: PositionSync) {
        positionRepository.findByPositionId(positionDto.id).ifPresentOrElse({
            it.title = positionDto.title
            it.deleted = positionDto.deleted
            it.state = positionDto.state
            positionRepository.save(it)
        }) {
            val projectPosition = Position(
                    null,
                    positionDto.id,
                    positionDto.title,
                    positionDto.deleted,
                    positionDto.state
            )
            positionRepository.save(projectPosition)
        }
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleActivation(positionId: Long) {
        val position = positionRepository.findByPositionId(positionId).orElseThrow()
        position.state = AggregateState.ACTIVE
        positionRepository.save(position)
    }

    @Transactional
    @Throws(UnexpectedRollbackException::class, Exception::class)
    override fun handleCompensation(positionId: Long, compensationPosition: PositionSync?) {
        if (compensationPosition != null) {
            positionRepository.findByPositionId(positionId).ifPresent {
                it.title = compensationPosition.title
                it.deleted = compensationPosition.deleted
                it.state = AggregateState.ACTIVE
                positionRepository.save(it)
            }
        } else {
            positionRepository.findByPositionId(positionId).ifPresent {
                positionRepository.deleteByPositionId(positionId)
            }
        }
    }

}