package seepick.localsportsclub.view

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.LscConfig
import seepick.localsportsclub.view.activity.ActivityViewModel
import seepick.localsportsclub.view.freetraining.FreetrainingViewModel
import seepick.localsportsclub.view.notes.NotesViewModel
import seepick.localsportsclub.view.preferences.PreferencesViewModel
import seepick.localsportsclub.view.shared.SharedModel
import seepick.localsportsclub.view.usage.UsageStorage
import seepick.localsportsclub.view.venue.VenueViewModel

fun viewModule(config: LscConfig) = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::VenueViewModel)
    viewModel { ActivityViewModel(get(), get(), get(), get(), get(), get(), get(), get(), config.syncDaysAhead) }
    viewModel { FreetrainingViewModel(get(), get(), get(), get(), get(), get(), get(), get(), config.syncDaysAhead) }
    viewModelOf(::NotesViewModel)
    viewModelOf(::SyncerViewModel)
    viewModelOf(::PreferencesViewModel)
    viewModelOf(::StatsViewModel)
    viewModelOf(::VersionNotifier)
    singleOf(::SharedModel)
    singleOf(::UsageStorage)
    singleOf(::SnackbarServiceViewModel) bind SnackbarService::class
}
