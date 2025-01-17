package dev.spikeysanju.expensetracker.app

import androidx.datastore.preferences.preferencesDataStore
import com.russhwolf.settings.datastore.DataStoreSettings
import dev.spikeysanju.expensetracker.data.local.AppDatabase
import dev.spikeysanju.expensetracker.main.UIModeDataStore
import dev.spikeysanju.expensetracker.repo.DriverFactory
import dev.spikeysanju.expensetracker.repo.TransactionRepo
import dev.spikeysanju.expensetracker.main.ExportCsvService
import dev.spikeysanju.expensetracker.about.AboutViewModel
import dev.spikeysanju.expensetracker.main.TransactionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val android.content.Context.themePrefDataStore by preferencesDataStore("ui_mode_pref")

val appModule = module {

    single {
        UIModeDataStore(
            factory = { DataStoreSettings(androidContext().themePrefDataStore) }
        )
    }

    single {
        AppDatabase(driver = DriverFactory(androidContext()).createDriver())
    }

    single {
        ExportCsvService(appContext = androidContext())
    }

    factory {
        TransactionRepo(db = get())
    }

    viewModel {
        AboutViewModel()
    }

    viewModel {
        TransactionViewModel(
            transactionRepo = get(),
            exportService = get(),
            uiModeDataStore = get()
        )
    }
}
