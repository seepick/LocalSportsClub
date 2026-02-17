package seepick.localsportsclub.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.v1.jdbc.Database
import java.sql.DriverManager

class LiquibaseMigratorTest : StringSpec() {
    init {
        "When migrate Then load and save works" {
//            reconfigureLog(
//                false, mapOf(
//                    "org.jetbrains.exposed" to ch.qos.logback.classic.Level.TRACE,
//                    "liquibase" to ch.qos.logback.classic.Level.TRACE,
//                )
//            )
            val jdbcUrl = testJdbcInmemoryUrl()
            val keepAliveConnection = DriverManager.getConnection(jdbcUrl)
            LiquibaseMigrator.migrate(LiquibaseConfig("", "", jdbcUrl))
            Database.connect(jdbcUrl, setupConnection = ::enableSqliteForeignKeySupport)

            val raw1 = Arb.venueDbo().next()
            val venue1 = ExposedVenueRepo.insert(raw1)
            ExposedVenueRepo.selectAllByCity(venue1.cityId).shouldBeSingleton().first() shouldBe venue1

            val raw2 = Arb.venueDbo().next()
            val venue2 = ExposedVenueRepo.insert(raw2)
            ExposedVenueLinksRepo.insert(VenueIdLink(venue1.id, venue2.id))

            val activity = Arb.activityDbo().next().copy(venueId = venue1.id)
            ExposedActivityRepo.insert(activity)

            val freetraining = Arb.freetrainingDbo().next().copy(venueId = venue1.id)
            ExposedFreetrainingRepo.insert(freetraining)

            ExposedSinglesRepo.select()
            ExposedSinglesRepo.insert(Arb.singlesDbo().next())
            ExposedSinglesRepo.update(Arb.singlesDbo().next())
            keepAliveConnection.close()
        }
    }
}
