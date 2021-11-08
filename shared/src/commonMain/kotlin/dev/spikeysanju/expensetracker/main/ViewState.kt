package dev.spikeysanju.expensetracker.main

import dev.spikeysanju.expensetracker.repo.TransactionModel

sealed class ViewState {
    object Loading : ViewState()
    object Empty : ViewState()
    data class Success(val transaction: List<TransactionModel>) : ViewState()
    data class Error(val exception: Throwable) : ViewState()
}
