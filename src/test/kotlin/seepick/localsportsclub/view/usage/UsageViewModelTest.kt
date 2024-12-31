package seepick.localsportsclub.view.usage

import app.cash.turbine.test
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import seepick.localsportsclub.StaticClock
import seepick.localsportsclub.UsageConfig
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.InMemoryActivityRepo
import seepick.localsportsclub.persistence.InMemoryFreetrainingRepo
import seepick.localsportsclub.persistence.activityDbo
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.usageConfig
import seepick.localsportsclub.uscConfig
import java.time.LocalDate
import java.time.LocalDateTime

class UsageViewModelTest : DescribeSpec() {

    private val year = 2025
    private lateinit var activityRepo: InMemoryActivityRepo
    private lateinit var freetrainingRepo: InMemoryFreetrainingRepo

    override suspend fun beforeEach(testCase: TestCase) {
        activityRepo = InMemoryActivityRepo()
        freetrainingRepo = InMemoryFreetrainingRepo()
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
    }

    private fun usage(
        today: String = "1.5.",
        withUsageConfig: UsageConfig.() -> UsageConfig,
    ) = UsageViewModel(
        clock = StaticClock(today.parseDateTime()),
        activityRepo = activityRepo,
        freetrainingRepo = freetrainingRepo,
        uscConfig = Arb.uscConfig().next().copy(
            usageConfig = Arb.usageConfig().next().let(withUsageConfig)
        ),
    )

    private fun String.parseDateTime(): LocalDateTime {
        val dates = this.substringBeforeLast(".").split(".")
        return LocalDateTime.of(year, dates[1].toInt(), dates[0].toInt(), 10, 0)
    }

    private fun String.parseDate(): LocalDate =
        this.parseDateTime().toLocalDate()

    private fun UsageViewModel.onActivityAdded(activityDbo: ActivityDbo) = apply {
        onActivityDbosAdded(listOf(activityDbo))
    }

    private fun activity(
        dateString: String,
        withActivityDbo: ActivityDbo.() -> ActivityDbo = { this },
    ): ActivityDbo {
        val start = dateString.parseDateTime()
        return Arb.activityDbo().next()
            .copy(
                from = start,
                to = start.plusHours(1),
            ).let(withActivityDbo)
    }

    init {
        describe("Period calculation") {
            it("reset ahead in same month") {
                usage(today = "4.10.") { copy(periodAlwaysFirstDay = 8) }
                    .also {
                        it.periodFirstDay shouldBeDate "8.9."
                        it.periodLastDay shouldBeDate "7.10."
                    }
            }
            it("reset before in same month") {
                usage(today = "15.10.") { copy(periodAlwaysFirstDay = 5) }
                    .also {
                        it.periodFirstDay shouldBeDate "5.10."
                        it.periodLastDay shouldBeDate "4.11."
                    }
            }
        }
        describe("When starting up") {
            it("Given fitting activity Then counter increased") {
                activityRepo.insert(activity("5.5.") { copy(wasCheckedin = true) })

                val usage = usage(today = "1.5.") { copy(periodAlwaysFirstDay = 1) }
                usage.onStartUp()

                usage checkedinCountShouldBe 1
            }
        }
        describe("Dynamic activity checked-in") {
            it("When fitting activity added Then counter increased") {
                val usage = usage(today = "15.5.") { copy(periodAlwaysFirstDay = 1) }

                usage.onActivityAdded(activity("5.5.") { copy(wasCheckedin = true) })

                usage checkedinCountShouldBe 1
            }
            it("Given fitting activity added When update making it unfitting Then counter decreased") {
                val usage = usage(today = "15.5.") { copy(periodAlwaysFirstDay = 1) }
                val activity = activity("5.5.") { copy(wasCheckedin = true) }
                usage.onActivityAdded(activity)

                usage.onActivityDboUpdated(activity.copy(wasCheckedin = false), ActivityFieldUpdate.WasCheckedin)

                usage checkedinCountShouldBe 0
            }
        }
    }

    private suspend infix fun UsageViewModel.checkedinCountShouldBe(expected: Int) {
        checkedinCount.test {
            awaitItem() shouldBe expected
        }
    }

    private infix fun LocalDate.shouldBeDate(date: String) {
        this shouldBe date.parseDate()
    }
}
