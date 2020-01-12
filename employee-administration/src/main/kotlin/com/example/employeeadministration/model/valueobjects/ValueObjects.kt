package com.example.employeeadministration.model.valueobjects

import java.util.*
import javax.persistence.Embeddable
import kotlin.Exception

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

/**
 * Value Object representing bank details.
 */
@Embeddable
data class BankDetails(val iban: String, val bic: String, val bankName: String)

/**
 * The mail vo provides means to create a (unique) company mail address using the name of an employee.
 * Should probably be moved to a service to check if the address already exists
 */
@Embeddable
data class CompanyMail(val mail: String) {

    private companion object {
        val domain = "company.com"
        fun createMailFromName(firstname: String, lastname: String): String {
            return "${firstname.first().toLowerCase()}.${lastname.toLowerCase()}@$domain"
        }
    }

    constructor(firstname: String, lastname: String) : this(createMailFromName(firstname, lastname))

}