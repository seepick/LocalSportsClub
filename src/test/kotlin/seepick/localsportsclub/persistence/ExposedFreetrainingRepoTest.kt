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

    private fun insertTrainingAndVenue(
        withTraining: FreetrainingDbo.() -> FreetrainingDbo = { this }
    ): FreetrainingDbo {
        val venue = venueRepo.insert(venue())
        val training = freetraining().copy(venueId = venue.id).let(withTraining)
        freetrainingRepo.insert(training)
        return training
    }

    init {
        extension(DbListener())

        describe("selectAll") {
            it("Given nothing Then is empty") {
                freetrainingRepo.selectAll().shouldBeEmpty()
            }
            it("Given one Then return it") {
                val training = insertTrainingAndVenue()

                freetrainingRepo.selectAll().shouldBeSingleton().first() shouldBe training
            }
        }
        describe("selectFutureMostDate") {
            it("Given none Return null") {
                freetrainingRepo.selectFutureMostDate().shouldBeNull()
            }
            it("Given some Return newest date") {
                insertTrainingAndVenue { copy(date = todayDate) }

                freetrainingRepo.selectFutureMostDate() shouldBe todayDate
            }
        }
        describe("insert") {
            it("Given no venue Then fail") {
                shouldThrow<ExposedSQLException> {
                    freetrainingRepo.insert(freetraining())
                }.shouldHaveCause { cause ->
                    cause.shouldBeInstanceOf<JdbcSQLIntegrityConstraintViolationException>().message.shouldContain("FK_FREETRAININGS_VENUE_ID")
                }
            }
            it("Given venue Then succeed") {
                val venue = venueRepo.insert(venue())
                val training = freetraining().copy(venueId = venue.id)

                freetrainingRepo.insert(training)

                freetrainingRepo.selectAll().shouldBeSingleton().first() shouldBe training
            }
        }
        describe("deleteNonCheckedinBefore") {
            it("Given old checkedin Then keep") {
                insertTrainingAndVenue { copy(date = yesterdayDate, state = FreetrainingState.Checkedin) }

                freetrainingRepo.deleteNonCheckedinBefore(todayDate)

                freetrainingRepo.selectAll().shouldBeSingleton()
            }
            it("Given old non-checkedin Then delete") {
                insertTrainingAndVenue { copy(date = yesterdayDate, state = FreetrainingState.Blank) }

                freetrainingRepo.deleteNonCheckedinBefore(todayDate)

                freetrainingRepo.selectAll().shouldBeEmpty()
            }
            it("Given newer non-checkedin Then keep") {
                insertTrainingAndVenue { copy(date = todayDate, state = FreetrainingState.Blank) }

                freetrainingRepo.deleteNonCheckedinBefore(todayDate)

                freetrainingRepo.selectAll().shouldBeSingleton()
            }
        }
    }
}
