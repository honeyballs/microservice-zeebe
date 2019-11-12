package com.example.employeeadministration

import com.example.employeeadministration.model.Employee
import com.example.employeeadministration.services.EmployeeService
import com.example.employeeadministration.services.WorkflowService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.math.BigDecimal

@SpringBootApplication
class EmployeeAdministrationApplication {

//    @Autowired
//    lateinit var employeeService: EmployeeService
//
//    @Bean
//    fun testRunner(): CommandLineRunner {
//        return CommandLineRunner {
//            Thread.sleep(3000)
//            var employee = Employee(null, "Max", "Mustermann", "Straße 1", "mail@domain.com", "1471204108274124", "Java Development", "Junior Developer", BigDecimal.TEN)
//            employee = employeeService.saveEmployeeWithWorkflow(employee, EmployeeService.Operation.CREATED)
//        }
//    }

}

fun main(args: Array<String>) {
    runApplication<EmployeeAdministrationApplication>(*args)

}
