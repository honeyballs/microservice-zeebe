package com.example.employeeadministration

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

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
