package seepick.localsportsclub.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldHaveSize
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
import seepick.localsportsclub.persistence.testInfra.persist
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
                repo.persist(Arb.venueDbo().next())
                transaction {
                    VenuesTable.selectAll().toList() shouldHaveSize 1
                }
            }
            it("Given same slug existing Then fail") {
                repo.persist(Arb.venueDbo().next())
                val ex = shouldThrow<ExposedSQLException> {
                    repo.persist(Arb.venueDbo().next().copy(id = 1, slug = "duplicate"))
                    repo.persist(Arb.venueDbo().next().copy(id = 2, slug = "duplicate"))
                }.shouldHaveCause { cause ->
                    cause.shouldBeInstanceOf<JdbcSQLIntegrityConstraintViolationException>()
                        .message shouldContain "VENUES_SLUG_UNIQUE_INDEX"
                }
            }
        }
        describe("When persist and select all") {
            it("Then returned") {
                val venue = Arb.venueDbo().next()
                repo.persist(venue)
                repo.selectAll().shouldBeSingleton().first() shouldBe venue
            }
        }
    }
}
