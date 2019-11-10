package com.example.employeeadministration.config

import io.zeebe.client.ZeebeClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ZeebeClientCreation {

    @Bean
    fun createClient(): ZeebeClient {
        val client = ZeebeClient.newClientBuilder()
                .brokerContactPoint("localhost:26500")
                .usePlaintext()
                .build()
        println("Client connected")
        return client
    }

}