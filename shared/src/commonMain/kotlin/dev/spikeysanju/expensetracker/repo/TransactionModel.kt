package dev.spikeysanju.expensetracker.repo

expect interface Serializable

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
    companion object
}
