package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileResolver
import java.io.File
import java.sql.Connection

private val log = logger {}

fun connectToDatabaseAndMigrate() {
    connect(dbDir = FileResolver.resolve(DirectoryEntry.Database), liquibaseEnabled = true)
}

fun connectToDatabaseEnvAware(isProd: Boolean) {
    val appDir = if (isProd) FileResolver.appDirectoryProd else FileResolver.appDirectoryDev
    connect(File(appDir, DirectoryEntry.Database.directoryName), liquibaseEnabled = false)
}

private fun connect(dbDir: File, liquibaseEnabled: Boolean) {
    val jdbcUrl = "jdbc:sqlite:${dbDir.absolutePath}/sqlite.db"
    log.info { "Connecting to database: $jdbcUrl" }
    if (liquibaseEnabled) {
        LiquibaseMigrator.migrate(LiquibaseConfig("", "", jdbcUrl))
    }
    Database.connect(
        url = jdbcUrl,
        setupConnection = ::enableSqliteForeignKeySupport,
        databaseConfig = DatabaseConfig {
            defaultMaxAttempts = 1
        }
    )
}

fun enableSqliteForeignKeySupport(connection: Connection) {
    connection.createStatement().use { statement ->
//        log.trace { "Enabling foreign key support for SQLite >> PRAGMA foreign_keys = ON" }
        statement.execute("PRAGMA foreign_keys = ON")
    }
}
