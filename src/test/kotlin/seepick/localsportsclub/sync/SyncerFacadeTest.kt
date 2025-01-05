package seepick.localsportsclub.sync

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.createDaysUntil
import seepick.localsportsclub.date
import seepick.localsportsclub.toLocalDateTime

class SyncerFacadeTest : DescribeSpec() {

    private val today = date(15)
    private val syncDaysAhead = 3
    private val anySyncDaysAhead = 2

    init {
        describe("calculateDaysToSync") {
            it("Given no last sync Then full range from today on") {
                val days = SyncerFacade.calculateDaysToSync(today, syncDaysAhead, lastSync = null)

                days shouldBe today.createDaysUntil(syncDaysAhead)
            }
            it("Given last sync was today Then do nothing") {
                val days = SyncerFacade.calculateDaysToSync(today, anySyncDaysAhead, lastSync = today.toLocalDateTime())

                days.shouldBeEmpty()
            }
            it("Given last sync was yesterday Then one day at range end") {
                val rangeOff = 1L
                val lastSync = today.minusDays(rangeOff)

                val days = SyncerFacade.calculateDaysToSync(today, syncDaysAhead, lastSync.toLocalDateTime())

                days shouldBe today.plusDays(syncDaysAhead - rangeOff).createDaysUntil(rangeOff.toInt())
            }
            it("Given last sync before sync range Then full range") {
                val days = SyncerFacade.calculateDaysToSync(
                    today, syncDaysAhead,
                    lastSync = today.minusDays(syncDaysAhead + 1L).toLocalDateTime()
                )

                days shouldBe today.createDaysUntil(syncDaysAhead)
            }
        }
    }
}
