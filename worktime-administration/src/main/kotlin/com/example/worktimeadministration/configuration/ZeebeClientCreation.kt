package com.example.worktimeadministration.configuration

import io.zeebe.client.ZeebeClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val datePattern = "dd.MM.yyyy"
const val dateTimePattern = "dd.MM.yyyy HH:mm:ss"

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