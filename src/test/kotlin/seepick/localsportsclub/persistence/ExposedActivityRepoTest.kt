package seepick.localsportsclub.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.nulls.shouldBeNull
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
import seepick.localsportsclub.persistence.testInfra.activityDbo
import seepick.localsportsclub.persistence.testInfra.venueDbo
import java.time.LocalDateTime

class ExposedActivityRepoTest : DescribeSpec() {
    private val activityRepo = ExposedActivityRepo
    private val venuesRepo = ExposedVenueRepo

    init {
        extension(DbListener())

        describe("When select all") {
            it("Then return empty") {
                activityRepo.selectAll().shouldBeEmpty()
            }
        }
        describe("When select future most date") {
            it("Given two different Then most future returned") {
                val venue = venuesRepo.insert(Arb.venueDbo().next())
                val today = LocalDateTime.now()
                val tomorrow = today.plusDays(1)
                activityRepo.insert(Arb.activityDbo().next().copy(from = tomorrow, venueId = venue.id))
                activityRepo.insert(Arb.activityDbo().next().copy(from = today, venueId = venue.id))
                activityRepo.selectFutureMostDate() shouldBe tomorrow.toLocalDate()
            }
            it("Given nothing Then null") {
                activityRepo.selectFutureMostDate().shouldBeNull()
            }
        }
        describe("When insert") {
            it("Given no venue Then fail") {
                val activity = Arb.activityDbo().next()
                shouldThrow<ExposedSQLException> {
                    activityRepo.insert(activity)
                }.shouldHaveCause { cause ->
                    cause.shouldBeInstanceOf<JdbcSQLIntegrityConstraintViolationException>()
                        .message.shouldContain("FK_ACTIVITIES_VENUE_ID")
                }
            }
            it("Given venue Then saved successfully") {
                val venue = venuesRepo.insert(Arb.venueDbo().next())
                val activity = Arb.activityDbo().next().copy(venueId = venue.id)
                activityRepo.insert(activity)
                transaction {
                    val stored = ActivitiesTable.selectAll().toList().shouldBeSingleton().first()
                    stored[ActivitiesTable.id].value shouldBe activity.id
                    stored[ActivitiesTable.name] shouldBe activity.name
                    // ...
                }
            }
            it("Given same ID existing Then fail") {
                val venue = venuesRepo.insert(Arb.venueDbo().next())
                val activity1 = Arb.activityDbo().next().copy(venueId = venue.id)
                val activity2 = Arb.activityDbo().next().copy(id = activity1.id)
                activityRepo.insert(activity1)

                shouldThrow<ExposedSQLException> {
                    activityRepo.insert(activity2)
                }.shouldHaveCause { cause ->
                    cause.shouldBeInstanceOf<JdbcSQLIntegrityConstraintViolationException>()
                        .message shouldContain "Unique index or primary key violation"
                }
            }
        }
        describe("When insert and select all") {
            it("Then returned") {
                val venue = venuesRepo.insert(Arb.venueDbo().next())
                val activity = Arb.activityDbo().next().copy(venueId = venue.id)
                activityRepo.insert(activity)
                activityRepo.selectAll().shouldBeSingleton().first() shouldBe activity
            }
        }
        describe("When update") {
            it("Given venue and activity Then update successful") {
                val venue = venuesRepo.insert(Arb.venueDbo().next())
                val activity = Arb.activityDbo().next().copy(venueId = venue.id)
                activityRepo.insert(activity)

                activityRepo.update(activity.copy(spotsLeft = activity.spotsLeft + 1))

                activityRepo.selectAll().shouldBeSingleton().first().spotsLeft shouldBe activity.spotsLeft + 1
            }
            it("Given no activity Then fail") {
                val activity = Arb.activityDbo().next()
                shouldThrow<IllegalStateException> {
                    activityRepo.update(activity)
                }.message shouldContain "Expected 1 to be updated by ID"
            }
        }
    }
}
