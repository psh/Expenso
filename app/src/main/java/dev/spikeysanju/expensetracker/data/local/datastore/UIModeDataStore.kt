package dev.spikeysanju.expensetracker.data.local.datastore

import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow

class UIModeDataStore(factory: () -> FlowSettings) : UIModeImpl {

    private val settings: FlowSettings = factory()

    // used to get the data from datastore
    override val uiMode: Flow<Boolean>
        get() = settings.getBooleanFlow(UI_MODE_KEY_NAME, false)

    // used to save the ui preference to datastore
    override suspend fun saveToDataStore(isNightMode: Boolean) {
        settings.putBoolean(UI_MODE_KEY_NAME, isNightMode)
    }

    companion object {
        private const val UI_MODE_KEY_NAME = "ui_mode"
    }
}

interface UIModeImpl {
    val uiMode: Flow<Boolean>
    suspend fun saveToDataStore(isNightMode: Boolean)
}
