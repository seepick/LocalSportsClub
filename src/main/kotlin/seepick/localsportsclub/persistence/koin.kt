package seepick.localsportsclub.persistence

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.DatabaseMode

fun persistenceModule(databaseMode: DatabaseMode) = module {
    when (databaseMode) {
        DatabaseMode.Exposed -> {
            connectToDatabaseAndMigrate()
            single { ExposedVenueRepo } bind VenueRepo::class
            single { ExposedVenueLinksRepo } bind VenueLinksRepo::class
            single { ExposedActivityRepo } bind ActivityRepo::class
            single { ExposedFreetrainingRepo } bind FreetrainingRepo::class
            single { ExposedSinglesRepo } bind SinglesRepo::class
        }

        DatabaseMode.InMemory -> {
            singleOf(::InMemoryVenueRepo) bind VenueRepo::class
            singleOf(::InMemoryVenueLinksRepo) bind VenueLinksRepo::class
            singleOf(::InMemoryActivityRepo) bind ActivityRepo::class
            singleOf(::InMemoryFreetrainingRepo) bind FreetrainingRepo::class
            singleOf(::InMemorySinglesRepo) bind SinglesRepo::class
        }
    }
}

