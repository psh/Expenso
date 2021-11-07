package dev.spikeysanju.expensetracker.utils.viewState

import dev.spikeysanju.expensetracker.repo.TransactionModel

sealed class DetailState {
    object Loading : DetailState()
    object Empty : DetailState()
    data class Success(val transaction: TransactionModel) : DetailState()
    data class Error(val exception: Throwable) : DetailState()
}
