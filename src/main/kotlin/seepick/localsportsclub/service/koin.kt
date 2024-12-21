package seepick.localsportsclub.service

import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig

fun serviceModule(config: AppConfig) = module {
    single {
        VenuesService(
            venuesRepo = get(),
            dispatcher = get(),
            baseUrl = config.usc.baseUrl,
        )
    }
    single {
        FileSystemImageStorage(
            venueImagesFolder = FileResolver.resolve(DirectoryEntry.VenueImages),
        )
    } bind (ImageStorage::class)
}
