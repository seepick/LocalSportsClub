package seepick.localsportsclub.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteException
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.someOther
import java.time.LocalDateTime

class ExposedActivityRepoTest : DescribeSpec() {

    private val activityRepo = ExposedActivityRepo
    private val venueRepo = ExposedVenueRepo
    private val todayTime = SystemClock.now()
    private val todayDate = todayTime.toLocalDate()
    private val yesterdayTime = todayTime.minusDays(1)
    private lateinit var testRepo: TestRepoFacade
    private val anyCityId = 19
    private val cityId = 20
    private val cityId1 = 21
    private val cityId2 = 22
    private fun venue() = Arb.venueDbo().next()
    private fun activity() = Arb.activityDbo().next()

    override suspend fun beforeEach(testCase: TestCase) {
        testRepo = TestRepoFacade(activityRepo, venueRepo)
    }

    init {
        extension(DbListener())

        describe("When select all") {
            it("Then return empty") {
                activityRepo.selectAll(cityId).shouldBeEmpty()
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
                    state = ActivityState.Checkedin,
                    from = todayTime.plusDays(7),
                    to = todayTime.plusDays(7).plusHours(1),
                    venueId = venue.id
                )
                val activity2 = activity().copy(
                    state = ActivityState.Checkedin,
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
                    state = ActivityState.Blank,
                    from = todayTime.plusDays(7),
                    to = todayTime.plusDays(7).plusHours(1),
                    venueId = venue.id
                )
                val activity2 = activity().copy(
                    state = ActivityState.Checkedin,
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
                }.cause.shouldNotBeNull().shouldBeInstanceOf<SQLiteException>().message.shouldContain("FOREIGN KEY")
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
                }.cause.shouldNotBeNull().shouldBeInstanceOf<SQLiteException>().message shouldContain "ACTIVITIES.ID"
            }
        }
        describe("When insert and select all") {
            it("Then returned") {
                val venue = venueRepo.insert(venue())
                val activity = activity().copy(venueId = venue.id)
                activityRepo.insert(activity)
                activityRepo.selectAll(venue.cityId).shouldBeSingleton().first() shouldBe activity
            }
            it("for different city Then empty") {
                val venue = venueRepo.insert(venue().copy(cityId = cityId1))
                val activity = activity().copy(venueId = venue.id)
                activityRepo.insert(activity)

                activityRepo.selectAll(cityId2).shouldBeEmpty()
            }
        }
        describe("When select all booked") {
            it("Given non-booked activity Then empty") {
                val venue = venueRepo.insert(venue().copy(cityId = cityId))
                val activity = activity().copy(venueId = venue.id, state = ActivityState.Blank)
                activityRepo.insert(activity)

                activityRepo.selectAllBooked(cityId).shouldBeEmpty()
            }
            it("Given booked activity Then return it") {
                val venue = venueRepo.insert(venue().copy(cityId = cityId))
                val activity = activity().copy(venueId = venue.id, state = ActivityState.Booked)
                activityRepo.insert(activity)

                activityRepo.selectAllBooked(cityId).shouldBeSingleton().first() shouldBe activity
            }
            it("Given booked activity for different city Then empty") {
                val venue = venueRepo.insert(venue().copy(cityId = cityId1))
                val activity = activity().copy(venueId = venue.id, state = ActivityState.Booked)
                activityRepo.insert(activity)

                activityRepo.selectAllBooked(cityId2).shouldBeEmpty()
            }
        }
        describe("When update") {
            it("Given venue and activity Then update all excluding venueId") {
                val venue1 = venueRepo.insert(venue())
                val venue2 = venueRepo.insert(venue())
                val activity = activity().copy(venueId = venue1.id, planId = 1)
                activityRepo.insert(activity)

                val updateActivity = ActivityDbo(
                    id = activity.id,
                    spotsLeft = activity.spotsLeft + 1,
                    teacher = "${activity.teacher} 2",
                    state = activity.state.someOther(),
                    name = activity.name + "2",
                    category = activity.category + "2",
                    from = activity.from.plusHours(1),
                    to = activity.to.plusHours(1),
                    venueId = venue2.id,
                    cancellationLimit = activity.cancellationLimit,
                    planId = 2,
                )
                activityRepo.update(updateActivity)

                activityRepo.selectAll(venue1.cityId).shouldBeSingleton()
                    .first() shouldBe updateActivity.copy(venueId = activity.venueId)
            }
            it("Given no activity Then fail") {
                shouldThrow<IllegalStateException> {
                    activityRepo.update(activity())
                }.message shouldContain "Expected 1 to be updated by ID"
            }
        }
        describe("When delete non-booked and non-checkedin before") {
            it("Given older but was checkedin Then keep") {
                testRepo.insertActivity(
                    state = ActivityState.Checkedin, from = yesterdayTime,
                    createVenue = true, cityId = cityId
                )

                val deleted = activityRepo.deleteBlanksBefore(todayDate)

                deleted.shouldBeEmpty()
                activityRepo.selectAll(cityId).shouldBeSingleton()
            }
            it("Given older but is booked Then keep") {
                testRepo.insertActivity(
                    state = ActivityState.Booked, from = yesterdayTime,
                    createVenue = true, cityId = cityId
                )

                val deleted = activityRepo.deleteBlanksBefore(todayDate)

                deleted.shouldBeEmpty()
                activityRepo.selectAll(cityId).shouldBeSingleton()
            }
            it("Given same date Then keep") {
                createActivityForDeletion(todayTime, cityId = cityId)

                val deleted = activityRepo.deleteBlanksBefore(todayDate)

                deleted.shouldBeEmpty()
                activityRepo.selectAll(cityId).shouldBeSingleton()
            }
            it("Given older date Then delete") {
                val activity = createActivityForDeletion(yesterdayTime, cityId = cityId)

                val deleted = activityRepo.deleteBlanksBefore(todayDate)

                activityRepo.selectAll(cityId).shouldBeEmpty()
                deleted shouldBeEqual listOf(activity)
            }
        }
    }

    private fun createActivityForDeletion(date: LocalDateTime, cityId: Int = anyCityId) =
        testRepo.insertActivity(
            state = ActivityState.Blank,
            from = date,
            createVenue = true,
            cityId = cityId,
        )
}
