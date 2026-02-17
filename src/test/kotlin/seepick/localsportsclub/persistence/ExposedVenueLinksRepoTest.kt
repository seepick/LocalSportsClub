package seepick.localsportsclub.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.sqlite.SQLiteException

class ExposedVenueLinksRepoTest : DescribeSpec() {

    private val cityId = 20
    private val cityId1 = 21
    private val cityId2 = 22
    private val venueRepo = ExposedVenueRepo
    private val linksRepo = ExposedVenueLinksRepo
    private val nonExistingVenueId1 = 41
    private val nonExistingVenueId2 = 42

    init {
        extension(DbListener())
        describe("selectAll") {
            it("Given link When city matches Then return it") {
                val (venueId1, venueId2) = insertVenuesForCities(cityId, cityId)
                linksRepo.insert(VenueIdLink(venueId1, venueId2))

                linksRepo.selectAll(cityId).shouldBeSingleton()
            }
            it("Given link When city mismatches Then return empty") {
                val (venueId1, venueId2) = insertVenuesForCities(cityId1, cityId1)
                linksRepo.insert(VenueIdLink(venueId1, venueId2))

                linksRepo.selectAll(cityId2).shouldBeEmpty()
            }
            it("Given link When city for venue1 mismatches Then return empty") {
                val (venueId1, venueId2) = insertVenuesForCities(cityId1, cityId2)
                linksRepo.insert(VenueIdLink(venueId1, venueId2))

                linksRepo.selectAll(cityId2).shouldBeEmpty()
            }
            it("Given link When city for venue2 mismatches Then return empty") {
                val (venueId1, venueId2) = insertVenuesForCities(cityId2, cityId1)
                linksRepo.insert(VenueIdLink(venueId1, venueId2))

                linksRepo.selectAll(cityId2).shouldBeEmpty()
            }
        }
        describe("When insert") {
            it("without venues Then fail") {
                shouldThrow<ExposedSQLException> {
                    linksRepo.insert(VenueIdLink(nonExistingVenueId1, nonExistingVenueId2))
                }.cause.shouldNotBeNull().shouldBeInstanceOf<SQLiteException>().message shouldContain "FOREIGN KEY"
            }
            it("Given both venues exist Then persisted") {
                val venue1 = venueRepo.insert(Arb.venueDbo().next())
                val venue2 = venueRepo.insert(Arb.venueDbo().next())

                linksRepo.insert(VenueIdLink(venue1.id, venue2.id))

                transaction {
                    VenueLinksTable.selectAll().toList() shouldHaveSize 1
                }
            }
            it("Given both venues and link exist When insert twice Then fail") {
                val venue1 = venueRepo.insert(Arb.venueDbo().next())
                val venue2 = venueRepo.insert(Arb.venueDbo().next())
                linksRepo.insert(VenueIdLink(venue1.id, venue2.id))

                shouldThrow<ExposedSQLException> {
                    linksRepo.insert(VenueIdLink(venue1.id, venue2.id))
                }.cause.shouldNotBeNull()
                    .shouldBeInstanceOf<SQLiteException>().message shouldContain "UNIQUE constraint failed"
            }
            it("Given both venues and link exist When insert twice in other order Then fail") {
                val venue1 = venueRepo.insert(Arb.venueDbo().next())
                val venue2 = venueRepo.insert(Arb.venueDbo().next())
                linksRepo.insert(VenueIdLink(venue1.id, venue2.id))

                shouldThrow<ExposedSQLException> {
                    linksRepo.insert(VenueIdLink(venue2.id, venue1.id))
                }.cause.shouldNotBeNull()
                    .shouldBeInstanceOf<SQLiteException>().message shouldContain "UNIQUE constraint failed"
            }
        }
    }

    private fun insertVenuesForCities(cityId1: Int, cityId2: Int): Pair<Int, Int> {
        val venueId1 = ExposedVenueRepo.insert(Arb.venueDbo().next().copy(cityId = cityId1)).id
        val venueId2 = ExposedVenueRepo.insert(Arb.venueDbo().next().copy(cityId = cityId2)).id
        return venueId1 to venueId2
    }
}
