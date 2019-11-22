package com.example.employeeadministration.config

import io.zeebe.client.ZeebeClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.get

/**
 * Creates a singleton ZeebeClient when the application context is initialized.
 * If no URL is defined as an environment variable (used in Docker) the URL defaults to localhost.
 */
@Configuration
class ZeebeClientCreation(val environment: Environment) {

    @Bean
    fun createClient(): ZeebeClient {
        val client = ZeebeClient.newClientBuilder()
                .brokerContactPoint(environment.getProperty("ZEEBE_URL", "localhost:26500"))
                .usePlaintext()
                .build()
        println("Client connected")
        return client
    }

}