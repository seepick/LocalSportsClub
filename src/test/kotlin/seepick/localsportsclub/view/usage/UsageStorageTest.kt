package seepick.localsportsclub.view.usage

import app.cash.turbine.test
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import seepick.localsportsclub.StaticClock
import seepick.localsportsclub.UsageConfig
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.InMemoryActivityRepo
import seepick.localsportsclub.persistence.InMemoryFreetrainingRepo
import seepick.localsportsclub.persistence.activityDbo
import seepick.localsportsclub.persistence.freetrainingDbo
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.usageConfig
import seepick.localsportsclub.uscConfig
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class UsageStorageTest : DescribeSpec() {

    private val year = 2025
    private lateinit var activityRepo: InMemoryActivityRepo
    private lateinit var freetrainingRepo: InMemoryFreetrainingRepo

    override suspend fun beforeEach(testCase: TestCase) {
        activityRepo = InMemoryActivityRepo()
        freetrainingRepo = InMemoryFreetrainingRepo()
    }

    private fun usage(
        today: String = "1.5.",
        withUsageConfig: UsageConfig.() -> UsageConfig,
    ) = UsageStorage(
        clock = StaticClock(today.parseDateWithFixedTime()),
        activityRepo = activityRepo,
        freetrainingRepo = freetrainingRepo,
        uscConfig = Arb.uscConfig().next().copy(
            usageConfig = Arb.usageConfig().next().let(withUsageConfig)
        ),
    )

    private fun String.parseDateWithFixedTime(): LocalDateTime = LocalDateTime.of(parseDate(), LocalTime.of(10, 0))

    private fun String.parseDate(): LocalDate = substringBeforeLast(".").split(".").let { dates ->
        LocalDate.of(year, dates[1].toInt(), dates[0].toInt())
    }

    private fun UsageStorage.onActivityAdded(activityDbo: ActivityDbo) = apply {
        onActivityDbosAdded(listOf(activityDbo))
    }

    private fun UsageStorage.onFreetrainingAdded(freetrainingDbo: FreetrainingDbo) = apply {
        onFreetrainingDbosAdded(listOf(freetrainingDbo))
    }

    private fun activity(
        dateString: String,
        withActivityDbo: ActivityDbo.() -> ActivityDbo = { this },
    ): ActivityDbo {
        val start = dateString.parseDateWithFixedTime()
        return Arb.activityDbo().next().copy(
            from = start,
            to = start.plusHours(1),
        ).let(withActivityDbo)
    }

    private fun freetraining(
        dateString: String,
        withFreetrainingDbo: FreetrainingDbo.() -> FreetrainingDbo = { this },
    ): FreetrainingDbo = Arb.freetrainingDbo().next().copy(
        date = dateString.parseDate(),
    ).let(withFreetrainingDbo)

    init {
        describe("Period calculation") {
            it("reset ahead in same month") {
                usage(today = "4.10.") { copy(periodConfiguredFirstDay = 8) }.also {
                    it.periodFirstDay shouldBeDate "8.9."
                    it.periodLastDay shouldBeDate "7.10."
                }
            }
            it("reset before in same month") {
                usage(today = "15.10.") { copy(periodConfiguredFirstDay = 5) }.also {
                    it.periodFirstDay shouldBeDate "5.10."
                    it.periodLastDay shouldBeDate "4.11."
                }
            }
        }
        describe("When starting up") {
            it("Given fitting activity Then checkedin counter increased") {
                activityRepo.insert(activity("2.5.") { copy(wasCheckedin = true) })

                val usage = usage(today = "15.5.") { copy(periodConfiguredFirstDay = 1) }
                usage.onStartUp()

                usage checkedinCountShouldBe 1
            }
            it("Given fitting freetraining Then checkedin counter increased") {
                freetrainingRepo.insert(freetraining("5.5.") { copy(wasCheckedin = true) })

                val usage = usage(today = "15.5.") { copy(periodConfiguredFirstDay = 1) }
                usage.onStartUp()

                usage checkedinCountShouldBe 1
            }
        }
        describe("activity checked-in") {
            it("When fitting activity added Then counter increased") {
                val usage = usage(today = "15.5.") { copy(periodConfiguredFirstDay = 1) }

                usage.onActivityAdded(activity("5.5.") { copy(wasCheckedin = true) })

                usage checkedinCountShouldBe 1
            }
            it("When too early activity added Then counter stays") {
                val usage = usage(today = "15.5.") { copy(periodConfiguredFirstDay = 1) }

                usage.onActivityAdded(activity("18.4.") { copy(wasCheckedin = true) })

                usage checkedinCountShouldBe 0
            }
            it("When too late activity added Then counter stays") {
                val usage = usage(today = "15.5.") { copy(periodConfiguredFirstDay = 1) }

                usage.onActivityAdded(activity("18.6.") { copy(wasCheckedin = true) })

                usage checkedinCountShouldBe 0
            }
            it("Given fitting activity added When update making it unfitting Then counter decreased") {
                val usage = usage(today = "15.5.") { copy(periodConfiguredFirstDay = 1) }
                val activity = activity("5.5.") { copy(wasCheckedin = true) }
                usage.onActivityAdded(activity)

                usage.onActivityDboUpdated(activity.copy(wasCheckedin = false), ActivityFieldUpdate.WasCheckedin)

                usage checkedinCountShouldBe 0
            }
        }
        describe("activity booked") {
            it("Given fitting activity Then booked counter increased") {
                activityRepo.insert(activity("20.5.") { copy(isBooked = true) })

                val usage = usage(today = "15.5.") { copy(periodConfiguredFirstDay = 1) }
                usage.onStartUp()

                usage bookedCountShouldBe 1
            }
        }
        describe("freetraining checked-in") {
            it("When fitting freetraining added Then checkedin counter increased") {
                val usage = usage(today = "15.5.") { copy(periodConfiguredFirstDay = 1) }

                usage.onFreetrainingAdded(freetraining("5.5.") { copy(wasCheckedin = true) })

                usage checkedinCountShouldBe 1
            }
        }
        describe("both together") {
            it("then added count") {
                val usage = usage(today = "15.5.") { copy(periodConfiguredFirstDay = 1) }

                usage.onActivityAdded(activity("4.5.") { copy(wasCheckedin = true) })
                usage.onFreetrainingAdded(freetraining("6.5.") { copy(wasCheckedin = true) })

                usage checkedinCountShouldBe 2
            }
        }
        describe("Percentages") {
            it("period") {
                fun percentagePeriodShouldBe(today: Int, expected: Double) {
                    usage(today = "$today.12.") { copy(periodConfiguredFirstDay = 1) }
                        .percentagePeriod.shouldBeBetween(expected, expected, 0.001)
                }
                percentagePeriodShouldBe(1, 0.0)
                percentagePeriodShouldBe(2, 0.032)
                percentagePeriodShouldBe(30, 0.935)
                percentagePeriodShouldBe(31, 0.967)
            }
            it("check-in") {
                suspend fun percentageCheckedinShouldBe(countCheckins: Int, expected: Double) {
                    usage(today = "1.12.") { copy(periodConfiguredFirstDay = 1, maxBookingsForPeriod = 10) }
                        .also { usage ->
                            repeat(countCheckins) {
                                usage.onFreetrainingAdded(freetraining("3.12.") { copy(wasCheckedin = true) })
                            }
                        }
                        .percentageCheckedinShouldBe(expected)
                }
                percentageCheckedinShouldBe(0, 0.0)
                percentageCheckedinShouldBe(1, 0.1)
                percentageCheckedinShouldBe(9, 0.9)
                percentageCheckedinShouldBe(10, 1.0)
            }
            it("booked") {
                suspend fun percentageBookedShouldBe(countCheckins: Int, expected: Double) {
                    usage(today = "1.12.") { copy(periodConfiguredFirstDay = 1, maxBookingsForPeriod = 10) }
                        .also { usage ->
                            repeat(countCheckins) {
                                usage.onActivityAdded(activity("3.12.") { copy(isBooked = true) })
                            }
                        }
                        .percentageBookedShouldBe(expected)
                }
                percentageBookedShouldBe(0, 0.0)
                percentageBookedShouldBe(1, 0.1)
                percentageBookedShouldBe(9, 0.9)
                percentageBookedShouldBe(10, 1.0)
            }
        }
    }

    private suspend infix fun UsageStorage.checkedinCountShouldBe(expected: Int) {
        checkedinCount.test {
            awaitItem() shouldBe expected
        }
    }

    private suspend infix fun UsageStorage.bookedCountShouldBe(expected: Int) {
        bookedCount.test {
            awaitItem() shouldBe expected
        }
    }

    private infix fun LocalDate.shouldBeDate(date: String) {
        this shouldBe date.parseDate()
    }

    private suspend infix fun UsageStorage.percentageCheckedinShouldBe(expected: Double) {
        percentageCheckedin.test {
            awaitItem().shouldBeBetween(expected, expected, 0.001)
        }
    }

    private suspend infix fun UsageStorage.percentageBookedShouldBe(expected: Double) {
        percentageBooked.test {
            awaitItem().shouldBeBetween(expected, expected, 0.001)
        }
    }
}
