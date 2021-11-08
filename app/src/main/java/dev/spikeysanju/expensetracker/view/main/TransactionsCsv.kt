package dev.spikeysanju.expensetracker.view.main

import com.opencsv.bean.CsvBindByName
import dev.spikeysanju.expensetracker.repo.TransactionModel
import dev.spikeysanju.expensetracker.utils.formatDate

data class TransactionsCSV(
    @CsvBindByName(column = "title")
    val title: String,
    @CsvBindByName(column = "amount")
    val amount: Double,
    @CsvBindByName(column = "transactionType")
    val transactionType: String,
    @CsvBindByName(column = "tag")
    val tag: String,
    @CsvBindByName(column = "date")
    val date: String,
    @CsvBindByName(column = "note")
    val note: String,
    @CsvBindByName(column = "createdAt")
    val createdAtDate: String
)

fun List<TransactionModel>.toCsv() = map {
    TransactionsCSV(
        title = it.title,
        amount = it.amount,
        transactionType = it.transactionType,
        tag = it.tag,
        date = it.date,
        note = it.note,
        createdAtDate = formatDate(it.createdAt),
    )
}


