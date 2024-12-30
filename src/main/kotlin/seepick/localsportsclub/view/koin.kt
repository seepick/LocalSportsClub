package seepick.localsportsclub.view

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.view.activity.ActivityViewModel
import seepick.localsportsclub.view.freetraining.FreetrainingViewModel
import seepick.localsportsclub.view.venue.VenueViewModel

fun viewModule(config: AppConfig) = module {
    viewModel {
        MainViewModel(
            syncer = get(),
            firstScreen = config.firstScreen,
        )
    }
    viewModelOf(::VenueViewModel)
    viewModelOf(::ActivityViewModel)
    viewModelOf(::FreetrainingViewModel)
}
