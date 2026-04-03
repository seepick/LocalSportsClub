package seepick.localsportsclub.persistence

import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.LscConfig
import seepick.localsportsclub.service.DirectoryEntry

fun exposedPersistenceModule(config: LscConfig) = module {
    connectToDatabaseAndMigrate(config.fileResolver.resolve(DirectoryEntry.Database))
    single { ExposedVenueRepo } bind VenueRepo::class
    single { ExposedVenueLinksRepo } bind VenueLinksRepo::class
    single { ExposedActivityRepo } bind ActivityRepo::class
    single { ExposedFreetrainingRepo } bind FreetrainingRepo::class
    single { ExposedSinglesRepo } bind SinglesRepo::class
    single { ExposedActivityRemarkRepo } bind ActivityRemarkRepo::class
    single { ExposedTeacherRemarkRepo } bind TeacherRemarkRepo::class
    single { GlobalRemarkExposedRepository } bind GlobalRemarkRepository::class
}
