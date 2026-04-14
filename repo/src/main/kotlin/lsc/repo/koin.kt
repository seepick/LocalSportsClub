package lsc.repo

import lsc.repo.internal.DummyRepoImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

public fun dummyRepoModule() = module {
    singleOf(::DummyRepoImpl) bind DummyRepo::class
}

public fun exposedPersistenceModule(dbDir: File) = module {
    connectToDatabaseAndMigrate(dbDir)
    single { ExposedVenueRepo } bind VenueRepo::class
    single { ExposedVenueLinksRepo } bind VenueLinksRepo::class
    single { ExposedActivityRepo } bind ActivityRepo::class
    single { ExposedFreetrainingRepo } bind FreetrainingRepo::class
    single { ExposedSinglesRepo } bind SinglesRepo::class
    single { ExposedActivityRemarkRepo } bind ActivityRemarkRepo::class
    single { ExposedTeacherRemarkRepo } bind TeacherRemarkRepo::class
    single { GlobalRemarkExposedRepository } bind GlobalRemarkRepository::class
}
