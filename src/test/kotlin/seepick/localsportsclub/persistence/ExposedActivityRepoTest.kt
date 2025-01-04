package seepick.localsportsclub.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.equals.shouldBeEqual
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
import seepick.localsportsclub.service.date.SystemClock
import java.time.LocalDateTime

class ExposedActivityRepoTest : DescribeSpec() {

    private val activityRepo = ExposedActivityRepo
    private val venueRepo = ExposedVenueRepo
    private val todayTime = SystemClock.now()
    private val todayDate = todayTime.toLocalDate()
    private val yesterdayTime = todayTime.minusDays(1)
    private lateinit var testRepo: TestRepoFacade

    private fun venue() = Arb.venueDbo().next()
    private fun activity() = Arb.activityDbo().next()

    override suspend fun beforeEach(testCase: TestCase) {
        testRepo = TestRepoFacade(activityRepo, venueRepo)
    }

    init {
        extension(DbListener())

        describe("When select all") {
            it("Then return empty") {
                activityRepo.selectAll().shouldBeEmpty()
            }
        }
        describe("When select future most date") {
            it("Given two different Then most future returned") {
                val venue = venueRepo.insert(venue())
                val tomorrow = todayTime.plusDays(1)
                activityRepo.insert(activity().copy(from = tomorrow, venueId = venue.id))
                activityRepo.insert(activity().copy(from = todayTime, venueId = venue.id))
                activityRepo.selectFutureMostDate() shouldBe tomorrow.toLocalDate()
            }
            it("Given nothing Then null") {
                activityRepo.selectFutureMostDate().shouldBeNull()
            }
        }
        describe("When select newest checkedin date") {
            it("Given nothing Then return null") {
                activityRepo.selectNewestCheckedinDate().shouldBeNull()
            }
            it("Given 2 checkedin Then return newest") {
                val venue = venueRepo.insert(venue())
                val activity1 = activity().copy(
                    wasCheckedin = true,
                    from = todayTime.plusDays(7),
                    to = todayTime.plusDays(7).plusHours(1),
                    venueId = venue.id
                )
                val activity2 = activity().copy(
                    wasCheckedin = true,
                    from = todayTime.plusDays(3),
                    to = todayTime.plusDays(3).plusHours(1),
                    venueId = venue.id
                )
                activityRepo.insert(activity1)
                activityRepo.insert(activity2)

                activityRepo.selectNewestCheckedinDate() shouldBe activity1.from.toLocalDate()
            }
            it("Given 1 old checkedin and 1 new non-checkedin Then return old one") {
                val venue = venueRepo.insert(venue())
                val activity1 = activity().copy(
                    wasCheckedin = false,
                    from = todayTime.plusDays(7),
                    to = todayTime.plusDays(7).plusHours(1),
                    venueId = venue.id
                )
                val activity2 = activity().copy(
                    wasCheckedin = true,
                    from = todayTime.plusDays(3),
                    to = todayTime.plusDays(3).plusHours(1),
                    venueId = venue.id
                )
                activityRepo.insert(activity1)
                activityRepo.insert(activity2)

                activityRepo.selectNewestCheckedinDate() shouldBe activity2.from.toLocalDate()
            }
        }
        describe("When insert") {
            it("Given no venue Then fail") {
                val activity = activity()
                shouldThrow<ExposedSQLException> {
                    activityRepo.insert(activity)
                }.shouldHaveCause { cause ->
                    cause.shouldBeInstanceOf<JdbcSQLIntegrityConstraintViolationException>().message.shouldContain("FK_ACTIVITIES_VENUE_ID")
                }
            }
            it("Given venue Then saved successfully") {
                val venue = venueRepo.insert(venue())
                val activity = activity().copy(venueId = venue.id)
                activityRepo.insert(activity)
                transaction {
                    val stored = ActivitiesTable.selectAll().toList().shouldBeSingleton().first()
                    stored[ActivitiesTable.id].value shouldBe activity.id
                    stored[ActivitiesTable.name] shouldBe activity.name
                    // ...
                }
            }
            it("Given same ID existing Then fail") {
                val venue = venueRepo.insert(venue())
                val activity1 = activity().copy(venueId = venue.id)
                val activity2 = activity().copy(id = activity1.id)
                activityRepo.insert(activity1)

                shouldThrow<ExposedSQLException> {
                    activityRepo.insert(activity2)
                }.shouldHaveCause { cause ->
                    cause.shouldBeInstanceOf<JdbcSQLIntegrityConstraintViolationException>().message shouldContain "Unique index or primary key violation"
                }
            }
        }
        describe("When insert and select all") {
            it("Then returned") {
                val venue = venueRepo.insert(venue())
                val activity = activity().copy(venueId = venue.id)
                activityRepo.insert(activity)
                activityRepo.selectAll().shouldBeSingleton().first() shouldBe activity
            }
        }
        describe("When update") {
            it("Given venue and activity Then update relevant fields") {
                val ignoredDate = SystemClock.now()
                val ignoredVenueId = 42
                val venue = venueRepo.insert(venue())
                val activity = activity().copy(venueId = venue.id)
                activityRepo.insert(activity)

                val updateActivity = ActivityDbo(
                    id = activity.id,
                    spotsLeft = activity.spotsLeft + 1,
                    teacher = "${activity.teacher} 2",
                    isBooked = !activity.isBooked,
                    wasCheckedin = !activity.wasCheckedin,
                    venueId = ignoredVenueId,
                    name = "ignored",
                    category = "ignored",
                    from = ignoredDate,
                    to = ignoredDate,
                )
                activityRepo.update(updateActivity)

                activityRepo.selectAll().shouldBeSingleton().first() shouldBe activity.copy(
                    spotsLeft = updateActivity.spotsLeft,
                    teacher = updateActivity.teacher,
                    isBooked = updateActivity.isBooked,
                    wasCheckedin = updateActivity.wasCheckedin,
                )
            }
            it("Given no activity Then fail") {
                shouldThrow<IllegalStateException> {
                    activityRepo.update(activity())
                }.message shouldContain "Expected 1 to be updated by ID"
            }
        }
        describe("When delete non-booked and non-checkedin before") {
            it("Given older but was checkedin Then keep") {
                testRepo.insertActivity(wasCheckedin = true, from = yesterdayTime, createVenue = true)

                val deleted = activityRepo.deleteNonBookedNonCheckedinBefore(todayDate)

                activityRepo.selectAll().shouldBeSingleton()
                deleted.shouldBeEmpty()
            }
            it("Given older but is booked Then keep") {
                testRepo.insertActivity(isBooked = true, from = yesterdayTime, createVenue = true)

                val deleted = activityRepo.deleteNonBookedNonCheckedinBefore(todayDate)

                activityRepo.selectAll().shouldBeSingleton()
                deleted.shouldBeEmpty()
            }
            it("Given same date Then keep") {
                createActivityForDeletion(todayTime)

                val deleted = activityRepo.deleteNonBookedNonCheckedinBefore(todayDate)
                deleted.shouldBeEmpty()

                activityRepo.selectAll().shouldBeSingleton()
            }
            it("Given older date Then delete") {
                val activity = createActivityForDeletion(yesterdayTime)

                val deleted = activityRepo.deleteNonBookedNonCheckedinBefore(todayDate)

                activityRepo.selectAll().shouldBeEmpty()
                deleted shouldBeEqual listOf(activity)
            }
        }
    }

    private fun createActivityForDeletion(date: LocalDateTime) =
        testRepo.insertActivity(
            wasCheckedin = false,
            isBooked = false,
            from = date,
            createVenue = true
        )
}
