package seepick.localsportsclub.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteException

class ExposedVenueRepoTest : DescribeSpec() {

    private val repo = ExposedVenueRepo
    private val anyCityId = 19
    private val cityId = 40
    private val cityId1 = 41
    private val cityId2 = 42

    init {
        extension(DbListener())

        describe("When select all") {
            it("Then return empty") {
                repo.selectAllByCity(anyCityId).shouldBeEmpty()
            }
        }
        describe("When insert") {
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
                }.cause.shouldNotBeNull().shouldBeInstanceOf<SQLiteException>().message shouldContain "VENUES.SLUG"
            }
        }
        describe("When insert and select all") {
            it("Then returned") {
                val venue = repo.insert(Arb.venueDbo().next().copy(cityId = cityId))
                repo.selectAllByCity(cityId).shouldBeSingleton().first() shouldBe venue
            }
            it("different city Then empty") {
                repo.insert(Arb.venueDbo().next().copy(cityId = cityId1))
                repo.selectAllByCity(cityId2).shouldBeEmpty()
            }
        }
        describe("When update") {
            it("Given nothing Then fail") {
                shouldThrow<Exception> {
                    repo.update(Arb.venueDbo().next().copy(id = 42))
                }
            }
            it("Given existing Then updated") {
                val venue = repo.insert(
                    Arb.venueDbo().next().copy(
                        notes = "notes1",
                        rating = 1,
                        cityId = cityId,
                    )
                )

                repo.update(
                    venue.copy(
                        notes = "notes2",
                        rating = 2,
                    )
                )

                repo.selectAllByCity(cityId).first().should {
                    it.notes shouldBe "notes2"
                    it.rating shouldBe 2
                }
            }
        }
    }
}
