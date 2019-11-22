package com.example.employeeadministration.services

import com.example.employeeadministration.model.Aggregate
import io.zeebe.client.ZeebeClient
import org.springframework.stereotype.Service

/**
 * This service is used to start a process instance. A reference to the ZeebeClient is injected.
 */
@Service
class WorkflowService(val client: ZeebeClient) {

    fun createWorkflowInstance(data: Map<String, Any>, workflowName: String) {
        client.newCreateInstanceCommand()
                .bpmnProcessId(workflowName)
                .latestVersion()
                .variables(data)
                .send()
                .join()
        println("Workflow $workflowName started")
    }

}