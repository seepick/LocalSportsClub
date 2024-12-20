package seepick.localsportsclub.view

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import seepick.localsportsclub.view.venue.VenueViewModel

fun viewModule() = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::VenueViewModel)
}
