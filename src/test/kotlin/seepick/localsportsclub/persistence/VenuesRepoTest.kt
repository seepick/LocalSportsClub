package seepick.localsportsclub.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.throwable.shouldHaveCause
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.persistence.testInfra.DbListener
import seepick.localsportsclub.persistence.testInfra.venueDbo

class VenuesRepoTest : DescribeSpec() {

    private val repo = ExposedVenuesRepo

    init {
        extension(DbListener())

        describe("When select all") {
            it("Then return empty") {
                repo.selectAll().shouldBeEmpty()
            }
        }
        describe("When persist") {
            it("Then saved") {
                val venue = Arb.venueDbo().next()
                val inserted = repo.insert(venue)
                transaction {
                    val stored = VenuesTable.selectAll().toList().shouldBeSingleton().first()
                    stored[VenuesTable.id].value shouldBe inserted.id
                    stored[VenuesTable.slug] shouldBe venue.slug
                    // ...
                }
            }
            it("Given same slug existing Then fail") {
                repo.insert(Arb.venueDbo().next().copy(slug = "duplicate"))
                shouldThrow<ExposedSQLException> {
                    repo.insert(Arb.venueDbo().next().copy(slug = "duplicate"))
                }.shouldHaveCause { cause ->
                    cause.shouldBeInstanceOf<JdbcSQLIntegrityConstraintViolationException>()
                        .message shouldContain "VENUES_SLUG_UNIQUE_INDEX"
                }
            }
        }
        describe("When persist and select all") {
            it("Then returned") {
                val venue = repo.insert(Arb.venueDbo().next())
                repo.selectAll().shouldBeSingleton().first() shouldBe venue
            }
        }
    }
}
