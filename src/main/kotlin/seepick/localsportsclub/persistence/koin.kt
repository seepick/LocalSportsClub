package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileResolver

private val log = logger {}

fun persistenceModule(config: AppConfig) = module {
    if (config.database == AppConfig.DatabaseMode.Exposed) {
        connectToDatabase()
    }
    when (config.database) {
        AppConfig.DatabaseMode.Exposed -> {
            single { ExposedVenueRepo } bind VenueRepo::class
            single { ExposedVenueLinksRepo } bind VenueLinksRepo::class
            single { ExposedActivityRepo } bind ActivityRepo::class
            single { ExposedFreetrainingRepo } bind FreetrainingRepo::class
            single { ExposedSinglesRepo } bind SinglesRepo::class
        }

        AppConfig.DatabaseMode.InMemory -> {
            singleOf(::InMemoryVenueRepo) bind VenueRepo::class
            singleOf(::InMemoryVenueLinksRepo) bind VenueLinksRepo::class
            singleOf(::InMemoryActivityRepo) bind ActivityRepo::class
            singleOf(::InMemoryFreetrainingRepo) bind FreetrainingRepo::class
            singleOf(::InMemorySinglesRepo) bind SinglesRepo::class
        }
    }
}

private fun connectToDatabase() {
    val dbDir = FileResolver.resolve(DirectoryEntry.Database)
    val jdbcUrl = "jdbc:h2:file:${dbDir.absolutePath}/h2"
    log.info { "Connecting to database: $jdbcUrl" }
    LiquibaseMigrator.migrate(LiquibaseConfig("", "", jdbcUrl))
    Database.connect(jdbcUrl, databaseConfig = DatabaseConfig {
//        useNestedTransactions = true
    })
}
