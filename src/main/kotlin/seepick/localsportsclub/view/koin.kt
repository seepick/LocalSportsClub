package seepick.localsportsclub.view

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import seepick.localsportsclub.view.activity.ActivityViewModel
import seepick.localsportsclub.view.freetraining.FreetrainingViewModel
import seepick.localsportsclub.view.notes.NotesViewModel
import seepick.localsportsclub.view.preferences.PreferencesViewModel
import seepick.localsportsclub.view.usage.UsageStorage
import seepick.localsportsclub.view.venue.VenueViewModel

fun viewModule() = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::VenueViewModel)
    viewModelOf(::ActivityViewModel)
    viewModelOf(::FreetrainingViewModel)
    viewModelOf(::NotesViewModel)
    viewModelOf(::SyncerViewModel)
    viewModelOf(::PreferencesViewModel)
    viewModelOf(::VersionNotifier)
    singleOf(::UsageStorage)
    singleOf(::SnackbarService)
}
