package seepick.localsportsclub.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.throwable.shouldHaveCauseOfType
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class VenueLinksRepoTest : StringSpec() {
    private val venueRepo = ExposedVenueRepo
    private val linksRepo = ExposedVenueLinksRepo
    private val nonExistingVenueId1 = 41
    private val nonExistingVenueId2 = 42

    init {
        extension(DbListener())

        "When insert without venues Then fail" {
            shouldThrow<ExposedSQLException> {
                linksRepo.insert(VenueIdLink(nonExistingVenueId1, nonExistingVenueId2))
            }.shouldHaveCauseOfType<JdbcSQLIntegrityConstraintViolationException>()
        }
        "Given both venues exist When insert Then persisted" {
            val venue1 = venueRepo.insert(Arb.venueDbo().next())
            val venue2 = venueRepo.insert(Arb.venueDbo().next())

            linksRepo.insert(VenueIdLink(venue1.id, venue2.id))

            transaction {
                VenueLinksTable.selectAll().toList() shouldHaveSize 1
            }
        }
        "Given both venues and link exist When insert Then fail" {
            val venue1 = venueRepo.insert(Arb.venueDbo().next())
            val venue2 = venueRepo.insert(Arb.venueDbo().next())
            linksRepo.insert(VenueIdLink(venue1.id, venue2.id))

            shouldThrow<ExposedSQLException> {
                linksRepo.insert(VenueIdLink(venue1.id, venue2.id))
            }.cause.shouldNotBeNull().shouldBeInstanceOf<JdbcSQLIntegrityConstraintViolationException>()
        }
        "Given both venues and link exist When insert in other order Then fail" {
            val venue1 = venueRepo.insert(Arb.venueDbo().next())
            val venue2 = venueRepo.insert(Arb.venueDbo().next())
            linksRepo.insert(VenueIdLink(venue1.id, venue2.id))

            shouldThrow<ExposedSQLException> {
                linksRepo.insert(VenueIdLink(venue2.id, venue1.id))
            }.cause.shouldNotBeNull().shouldBeInstanceOf<JdbcSQLIntegrityConstraintViolationException>()
        }
    }
}
