package dev.spikeysanju.expensetracker.repo

import dev.spikeysanju.expensetracker.data.local.AppDatabase
import dev.spikeysanju.expensetracker.data.local.Transactions
import dev.spikeysanju.expensetracker.data.local.TransactionsQueries
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class TransactionRepo(private val db: AppDatabase) {
    private val queries: TransactionsQueries get() = db.transactionsQueries

    // insert transaction
    fun insert(transaction: TransactionModel) = with(transaction) {
        queries.insertTransaction(title, amount, transactionType, tag, date, note, createdAt)
    }

    // update transaction
    fun update(transaction: TransactionModel) = with(transaction) {
        queries.updateTransactionById(title, amount, transactionType, tag, date, note, createdAt, id)
    }

    // delete transaction
    fun delete(transaction: TransactionModel) =
        queries.deleteTransactionById(transaction.id)

    // get all transaction
    fun getAllTransactions() = flowOf(queries.allTransations().executeAsList())
        .map { it.map { e -> e.toModel() } }

    // get single transaction type - Expense or Income or else overall
    fun getAllSingleTransaction(transactionType: String) = flowOf(
        if (transactionType == "Overall") {
            queries.allTransations().executeAsList()
        } else {
            queries.allTransationsByType(transactionType).executeAsList()
        }
    ).map { it.map { e -> e.toModel() } }

    // get transaction by ID
    fun getByID(id: Int) = flowOf(queries.getTransactionById(id).executeAsOne())
        .map { it.toModel() }

    // delete transaction by ID
    fun deleteByID(id: Int) =
        queries.deleteTransactionById(id)

    private fun Transactions.toModel() =
        TransactionModel(id, title, amount, transactionType, tag, date, note, createdAt)
}
