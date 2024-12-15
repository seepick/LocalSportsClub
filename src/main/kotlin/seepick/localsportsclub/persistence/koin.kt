package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.util.reflect.instanceOf
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.logic.DirectoryEntry
import seepick.localsportsclub.logic.FileResolver

private val log = logger {}

fun persistenceModule(config: AppConfig) = module {
    if (config.database == AppConfig.DatabaseMode.Exposed) {
        connectToDatabase()
    }
    when(config.database) {
        AppConfig.DatabaseMode.Exposed -> {
            single { ExposedVenuesRepo } bind VenuesRepo::class
        }
        AppConfig.DatabaseMode.InMemory -> {
            singleOf(::InMemoryVenuesRepo) bind VenuesRepo::class
        }
    }
}

private fun connectToDatabase() {
    val dbDir = FileResolver.resolve(DirectoryEntry.Database)
    val jdbcUrl = "jdbc:h2:file:${dbDir.absolutePath}/h2"
    log.info { "Connecting to database: $jdbcUrl" }
    LiquibaseMigrator.migrate(LiquibaseConfig("", "", jdbcUrl))
    Database.connect(jdbcUrl, databaseConfig = DatabaseConfig {
    })
}
