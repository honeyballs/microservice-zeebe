package com.example.employeeadministration.services

import com.example.employeeadministration.model.Aggregate
import io.zeebe.client.ZeebeClient
import org.springframework.stereotype.Service

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