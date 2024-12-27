package seepick.localsportsclub.sync

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.TestableClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateUtilKtTest : DescribeSpec() {

    private val today = LocalDate.of(2024, 12, 5)
    private val todayNow = LocalDateTime.of(today, LocalTime.of(12, 0, 0))
    private val syncDaysAhead = 4
    private lateinit var clock: TestableClock
    override suspend fun beforeEach(testCase: TestCase) {
        clock = TestableClock(todayNow)
    }

    init {
        describe("When get days to sync") {
            /*
            today: 5.12.
            daysToSync = 4 -> max = 8.12.

            futurest: null  -> [ 5.12., 6.12., 7.12., 8.12. ]
            futurest: 1.12. -> [ 5.12., 6.12., 7.12., 8.12. ]
            futurest: 4.12. -> [ 5.12., 6.12., 7.12., 8.12. ]
            futurest: 5.12. -> [ 6.12., 7.12., 8.12. ]
            futurest: 6.12. -> [ 7.12., 8.12. ]
            futurest: 7.12. -> [ 8.12. ]
            futurest: 8.12. -> [ ]
            futurest: 9.12. -> [ ]
             */
            it("Given nothing Then return sync days ahead") {
                clock.daysUntil(syncDaysAhead, null).shouldBe(
                    (1..syncDaysAhead).map { todayNow.plusDays(it - 1L).toLocalDate() }
                )
            }
            it("Given date in past Then return sync days ahead") {
                clock.daysUntil(syncDaysAhead, today.minusDays(syncDaysAhead + 1L))
                    .shouldHaveSize(syncDaysAhead)
            }
            it("Given one day ahead Then return almost sync days ahead") {
                clock.daysUntil(syncDaysAhead, today.plusDays(1))
                    .shouldHaveSize(syncDaysAhead - 1)
            }
            it("Given almost but two ahead Then return one") {
                clock.daysUntil(syncDaysAhead, today.plusDays(syncDaysAhead - 1L)).shouldBeSingleton()
                    .first() shouldBe todayNow.plusDays(syncDaysAhead - 1L).toLocalDate()
            }
            it("Given overly ahead Then return empty") {
                clock.daysUntil(syncDaysAhead, today.plusDays(syncDaysAhead.toLong())).shouldBeEmpty()
            }
        }
    }
}
