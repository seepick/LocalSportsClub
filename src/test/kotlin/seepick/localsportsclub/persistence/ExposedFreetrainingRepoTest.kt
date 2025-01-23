package seepick.localsportsclub.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.sqlite.SQLiteException
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.model.FreetrainingState

class ExposedFreetrainingRepoTest : DescribeSpec() {

    private val freetrainingRepo = ExposedFreetrainingRepo
    private val venueRepo = ExposedVenueRepo
    private val todayTime = SystemClock.now()
    private val todayDate = todayTime.toLocalDate()
    private val yesterdayDate = todayDate.minusDays(1)
    private fun venue() = Arb.venueDbo().next()
    private fun freetraining() = Arb.freetrainingDbo().next()
    private val anyCityId = 19
    private val cityId = 20
    private val cityId1 = 21
    private val cityId2 = 22

    private fun insertTrainingAndVenue(
        cityId: Int = anyCityId,
        withTraining: FreetrainingDbo.() -> FreetrainingDbo = { this },
    ): FreetrainingDbo {
        val venue = venueRepo.insert(venue().copy(cityId = cityId))
        val training = freetraining().copy(venueId = venue.id).let(withTraining)
        freetrainingRepo.insert(training)
        return training
    }

    init {
        extension(DbListener())

        describe("selectAll") {
            it("Given nothing Then is empty") {
                freetrainingRepo.selectAll(anyCityId).shouldBeEmpty()
            }
            it("Given one Then return it") {
                val training = insertTrainingAndVenue(cityId = cityId)

                freetrainingRepo.selectAll(cityId).shouldBeSingleton().first() shouldBe training
            }
            it("When different city Then empty") {
                insertTrainingAndVenue(cityId = cityId1)

                freetrainingRepo.selectAll(cityId2).shouldBeEmpty()
            }
        }
        describe("selectAllScheduled") {
            it("Given non-matching Then empty") {
                insertTrainingAndVenue(cityId = cityId) { copy(state = FreetrainingState.Blank) }

                freetrainingRepo.selectAllScheduled(cityId).shouldBeEmpty()
            }
            it("Given matching Then return it") {
                insertTrainingAndVenue(cityId = cityId) { copy(state = FreetrainingState.Scheduled) }

                freetrainingRepo.selectAllScheduled(cityId).shouldBeSingleton()
            }
            it("When different city Then empty") {
                insertTrainingAndVenue(cityId = cityId) { copy(state = FreetrainingState.Scheduled) }

                freetrainingRepo.selectAllScheduled(cityId2).shouldBeEmpty()
            }
        }
        describe("selectFutureMostDate") {
            it("Given none Then return null") {
                freetrainingRepo.selectFutureMostDate(anyCityId).shouldBeNull()
            }
            it("Given some Then return newest date") {
                insertTrainingAndVenue(cityId) { copy(date = todayDate) }

                freetrainingRepo.selectFutureMostDate(cityId) shouldBe todayDate
            }
            it("When different city Then empty") {
                insertTrainingAndVenue(cityId1) { copy(date = todayDate) }

                freetrainingRepo.selectFutureMostDate(cityId2).shouldBeNull()
            }
        }
        describe("insert") {
            it("Given no venue Then fail") {
                shouldThrow<ExposedSQLException> {
                    freetrainingRepo.insert(freetraining())
                }.cause.shouldNotBeNull().shouldBeInstanceOf<SQLiteException>().message.shouldContain("FOREIGN KEY")
            }
            it("Given venue Then succeed") {
                val venue = venueRepo.insert(venue())
                val training = freetraining().copy(venueId = venue.id)

                freetrainingRepo.insert(training)

                freetrainingRepo.selectAll(venue.cityId).shouldBeSingleton().first() shouldBe training
            }
        }
        describe("deleteNonCheckedinBefore") {
            it("Given old checkedin Then keep") {
                insertTrainingAndVenue(cityId = cityId) {
                    copy(
                        date = yesterdayDate,
                        state = FreetrainingState.Checkedin
                    )
                }

                freetrainingRepo.deleteNonCheckedinBefore(todayDate)

                freetrainingRepo.selectAll(cityId).shouldBeSingleton()
            }
            it("Given old non-checkedin Then delete") {
                insertTrainingAndVenue(cityId = cityId) { copy(date = yesterdayDate, state = FreetrainingState.Blank) }

                freetrainingRepo.deleteNonCheckedinBefore(todayDate)

                freetrainingRepo.selectAll(cityId).shouldBeEmpty()
            }
            it("Given newer non-checkedin Then keep") {
                insertTrainingAndVenue(cityId = cityId) { copy(date = todayDate, state = FreetrainingState.Blank) }

                freetrainingRepo.deleteNonCheckedinBefore(todayDate)

                freetrainingRepo.selectAll(cityId).shouldBeSingleton()
            }
        }
    }
}
