package seepick.localsportsclub.service

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.LscConfig
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.service.singles.SinglesServiceImpl

fun serviceModule(config: LscConfig) = module {
    single { buildHttpClient() }
    single { SystemClock } bind Clock::class
    single {
        DataStorage(get(), get(), get(), get(), get(), get(), config.baseUrl)
    }
    singleOf(::SinglesServiceImpl) bind SinglesService::class
    singleOf(::BookingService)
    singleOf(::BookingValidator)
    singleOf(::ActivityDetailService)
    single {
        if (config.versionCheckEnabled) OnlineVersionChecker(get()) else NoopVersionChecker
    } bind VersionChecker::class
    single {
        FileSystemImageStorage(
            venueImagesFolder = FileResolver.resolve(DirectoryEntry.VenueImages),
        )
    } bind (ImageStorage::class)
}
