package dev.spikeysanju.expensetracker.repo

import java.io.Serializable
import java.text.DateFormat

data class TransactionModel(
    val id: Int,
    val title: String,
    val amount: Double,
    val transactionType: String,
    val tag: String,
    val date: String,
    val note: String,
    val createdAt: Long
) : Serializable {
    val createdAtDateFormat: String
        get() = DateFormat.getDateTimeInstance()
            .format(createdAt) // Date Format: Jan 11, 2021, 11:30 AM

    companion object
}
