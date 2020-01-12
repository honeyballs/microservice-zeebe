package com.example.employeeadministration.model.aggregates

import com.example.employeeadministration.model.valueobjects.Address
import com.example.employeeadministration.model.valueobjects.BankDetails
import com.example.employeeadministration.model.valueobjects.CompanyMail
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import javax.persistence.*

/**
 * The Employee aggregate.
 */
@Entity
class Employee(
        id: Long?,
        var firstname: String,
        var lastname: String,
        val birthday: LocalDate,
        @Embedded var address: Address,
        @Embedded var bankDetails: BankDetails,
        @ManyToOne(cascade = [CascadeType.REFRESH]) @JoinColumn(name = "fk_department") var department: Department,
        @ManyToOne(cascade = [CascadeType.REFRESH]) @JoinColumn(name = "fk_position") var position: Position,
        var availableVacationHours: Int,
        hourlyRate: BigDecimal,
        companyMail: CompanyMail?,
        deleted: Boolean = false,
        state: AggregateState = AggregateState.PENDING
): Aggregate(id, state, deleted) {

    // initialize it rounded. Apparently the custom setter is not applied to the initialization
    var hourlyRate: BigDecimal = hourlyRate.setScale(2, RoundingMode.HALF_UP)
        set(value) {
            // Always round the salary field
            field = value.setScale(2, RoundingMode.HALF_UP)
        }

    @Embedded
    var companyMail = companyMail ?: CompanyMail(firstname, lastname)

    fun changeName(firstname: String?, lastname: String?) {
        this.firstname = firstname ?: this.firstname
        this.lastname = lastname ?: this.lastname
        this.companyMail = CompanyMail(this.firstname, this.lastname)
    }

    fun moveToNewAddress(address: Address) {
        this.address = address
    }

    fun receiveRaiseBy(raiseAmount: BigDecimal) {
        this.hourlyRate = hourlyRate.add(raiseAmount)
    }

    fun switchBankDetails(bankDetails: BankDetails) {
        this.bankDetails = bankDetails
    }

    fun changeJobPosition(position: Position, newSalary: BigDecimal?) {
        this.position = position
        this.hourlyRate = newSalary ?: position.minHourlyWage
    }

    fun moveToAnotherDepartment(department: Department) {
        this.department = department
    }



    override fun deleteAggregate() {
        this.deleted = true
    }

    override fun copy(): Employee {
        return Employee(
                this.id!!,
                this.firstname,
                this.lastname,
                this.birthday,
                this.address,
                this.bankDetails,
                this.department.copy(),
                this.position.copy(),
                this.availableVacationHours,
                this.hourlyRate,
                this.companyMail,
                this.deleted,
                this.state
        )
    }

}