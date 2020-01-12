package com.example.projectadministration.model.aggregates

import java.lang.Exception
import java.util.*
import javax.persistence.*


@Entity
class Customer(
        id: Long?,
        var customerName: String,
        @Embedded var address: Address,
        @Embedded var contact: CustomerContact,
        deleted: Boolean = false,
        state: AggregateState = AggregateState.PENDING
): Aggregate(id, state, deleted) {

    fun changeCustomerContact(contact: CustomerContact) {
        this.contact = contact
    }

    fun moveCompanyLocation(address: Address) {
        this.address = address
    }

    fun changeName(name: String) {
        this.customerName = name
    }

    override fun deleteAggregate() {
        this.deleted = true
    }

    override fun copy(): Customer {
        return Customer(
                this.id,
                this.customerName,
                this.address,
                this.contact,
                this.deleted,
                this.state
        )
    }


}

@Embeddable
data class CustomerContact(val firstname: String, val lastname: String, val mail: String, val phone: String)


/**
 * Wrapper class for ZipCode which checks if the provided number matches the required length.
 * Not sure if necessary for DDD or if validation suffices.
 *
 */
@Embeddable
data class ZipCode(val zip: Int) {

    companion object {
        val ALLOWED_LENGTHS_PER_COUNTRY = hashMapOf<Locale, Int>(Pair(Locale.GERMANY, 5))
    }

    init {
        if (zip.toString().length != ALLOWED_LENGTHS_PER_COUNTRY[Locale.GERMANY]) throw Exception("The zip code provided does not match the required length of ${ALLOWED_LENGTHS_PER_COUNTRY[Locale.GERMANY]} digits.")
    }

}

/**
 * Value Object representing an address.
 */
@Embeddable
data class Address(val street: String, val no: Int, val city: String, val zipCode: ZipCode)