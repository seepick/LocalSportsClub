package seepick.localsportsclub.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.sql.Database
import seepick.localsportsclub.persistence.testInfra.buildTestJdbcUrl
import seepick.localsportsclub.persistence.testInfra.venueDbo

class LiquibaseMigratorTest : StringSpec() {
    init {
        "When migrate Then load and save works" {
            val jdbcUrl = buildTestJdbcUrl("liquitest")
            LiquibaseMigrator.migrate(LiquibaseConfig("", "", jdbcUrl))
            Database.connect(jdbcUrl)

            val venue = Arb.venueDbo().next()
            val inserted = ExposedVenuesRepo.insert(venue)
            ExposedVenuesRepo.selectAll().shouldBeSingleton().first() shouldBe venue.copy(id = inserted.id)
        }
    }
}
