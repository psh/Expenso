package dev.spikeysanju.expensetracker.view.main

import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow

class UIModeDataStore(factory: () -> FlowSettings) {

    private val settings: FlowSettings = factory()

    // used to get the data from datastore
    val uiMode: Flow<Boolean>
        get() = settings.getBooleanFlow(UI_MODE_KEY_NAME, false)

    // used to save the ui preference to datastore
    suspend fun saveToDataStore(isNightMode: Boolean) {
        settings.putBoolean(UI_MODE_KEY_NAME, isNightMode)
    }

    companion object {
        private const val UI_MODE_KEY_NAME = "ui_mode"
    }
}
