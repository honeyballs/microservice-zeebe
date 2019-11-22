package com.example.employeeadministration.config

import io.zeebe.client.ZeebeClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PreDestroy

/**
 * Uses the injected instance of the ZeebeClient to deploy workflows.
 * The workflows are under the resources directory.
 */
@Configuration
class ZeebeWorkflowDeployment {

    @Autowired
    lateinit var client: ZeebeClient

    @Bean
    fun deployWorkflows(): CommandLineRunner {
        return CommandLineRunner {
            val deploymentEvent = client.newDeployCommand()
                    .addResourceFromClasspath("synchronize-employee.bpmn")
                    .send()
                    .join()
            println("Workflows deployed")
        }
    }

    @PreDestroy
    fun closeClient() {
        client.close()
    }

}