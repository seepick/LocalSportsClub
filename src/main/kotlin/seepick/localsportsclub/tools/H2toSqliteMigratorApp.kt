package seepick.localsportsclub.tools

import ch.qos.logback.classic.Level
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.persistence.ActivitiesTableOld
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ExposedActivityRepo
import seepick.localsportsclub.persistence.ExposedFreetrainingRepo
import seepick.localsportsclub.persistence.ExposedVenueLinksRepo
import seepick.localsportsclub.persistence.ExposedVenueRepo
import seepick.localsportsclub.persistence.LiquibaseConfig
import seepick.localsportsclub.persistence.LiquibaseMigrator
import seepick.localsportsclub.persistence.VenuesTable
import seepick.localsportsclub.reconfigureLog
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.model.City

class H2toSqliteMigratorApp(
    private val h2: Database,
    private val sqlite: Database,
) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            reconfigureLog(
                useFileAppender = false, packageSettings = mapOf(
                    "seepick.localsportsclub" to Level.TRACE,
                    "liquibase" to Level.INFO,
                    "Exposed" to Level.INFO,
                )
            )

            val dbDir = FileResolver.resolve(DirectoryEntry.Database)

            val sqliteUrl = "jdbc:sqlite:${dbDir.absolutePath}/sqlite.db"
            val sqlite = Database.connect(sqliteUrl)
            LiquibaseMigrator.migrate(LiquibaseConfig("", "", sqliteUrl))

            val h2 = Database.connect("jdbc:h2:file:${dbDir.absolutePath}/h2")
            H2toSqliteMigratorApp(h2 = h2, sqlite = sqlite).migrate()
        }
    }

    private val cityId = City.Amsterdam.id

    private fun migrate() {
        migrateVenues()
        migrateVenueLinks()
        migrateActivities()
        migrateFreetrainings()
        // no need to migrate singles
    }

    private fun migrateVenues() {
        ExposedVenueRepo.db = h2
        val venues = ExposedVenueRepo.selectAll(cityId)
        println("venues.size (H2): ${venues.size}")

        ExposedVenueRepo.db = sqlite
        venues.forEach { venue ->
            ExposedVenueRepo.insert(venue)
        }
    }

    private fun migrateVenueLinks() {
        ExposedVenueLinksRepo.db = h2
        val venueLinks = ExposedVenueLinksRepo.selectAll(cityId).toSet()
        println("venueLinks.size (H2): ${venueLinks.size}")

        ExposedVenueLinksRepo.db = sqlite
        venueLinks.forEach { link ->
            ExposedVenueLinksRepo.insert(link)
        }
    }

    private fun migrateActivities() {
        ExposedActivityRepo.db = h2
        val activities = transaction(h2) {
            ActivitiesTableOld
                .join(VenuesTable, JoinType.LEFT, onColumn = ActivitiesTableOld.venueId, otherColumn = VenuesTable.id)
                .selectAll().where { VenuesTable.cityId.eq(cityId) }.orderBy(ActivitiesTableOld.from).map {
                    ActivityDbo.fromRowOld(it)
                }
        }
        println("activities.size (H2): ${activities.size}")

        ExposedActivityRepo.db = sqlite
        activities.forEach { activity ->
            ExposedActivityRepo.insert(activity)
        }
    }

    private fun migrateFreetrainings() {
        ExposedFreetrainingRepo.db = h2
        val freetrainings = ExposedFreetrainingRepo.selectAll(cityId)
        println("freetrainings.size (H2): ${freetrainings.size}")

        ExposedFreetrainingRepo.db = sqlite
        freetrainings.forEach { freetraining ->
            ExposedFreetrainingRepo.insert(freetraining)
        }
    }
}
