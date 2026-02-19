package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import java.io.File
import java.sql.Connection

private val log = logger {}

fun connectToDatabaseAndMigrate(
    dbDir: File, // = fileResolver.resolve(DirectoryEntry.Database)
) {
    connect(dbDir = dbDir, liquibaseEnabled = true)
}

fun connect(
    dbDir: File, // = fileResolver.resolve(DirectoryEntry.Database)
    liquibaseEnabled: Boolean,
) {
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
