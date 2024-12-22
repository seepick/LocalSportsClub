package seepick.localsportsclub.service

import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.service.model.DataStorage

fun serviceModule(config: AppConfig) = module {
    single {
        DataStorage(
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
