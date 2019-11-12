package com.example.worktimeadministration.configuration

import io.zeebe.client.ZeebeClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PreDestroy

@Configuration
class ZeebeWorkflowDeployment {

    @Autowired
    lateinit var client: ZeebeClient

//    @Bean
//    fun deployWorkflows(): CommandLineRunner {
//        return CommandLineRunner {
//            val deploymentEvent = client.newDeployCommand()
//                    .addResourceFromClasspath("employee-created.bpmn")
//                    .send()
//                    .join()
//            println("Workflows deployed")
//        }
//    }

    @PreDestroy
    fun closeClient() {
        client.close()
    }

}