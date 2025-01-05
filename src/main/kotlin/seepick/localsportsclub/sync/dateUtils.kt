package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.date.Clock
import java.time.LocalDate

private val log = logger {}

fun Clock.daysUntil(syncDaysAhead: Int, futureMostDate: LocalDate?): List<LocalDate> {
    log.debug { "daysUntil(syncDaysAhead=$syncDaysAhead, futureMostDate=$futureMostDate)" }
    val today = today()
    val furthestAwaySyncDayIncl = today.plusDays(syncDaysAhead.toLong() - 1)
    val futureMostActivity = futureMostDate ?: today.minusDays(1)
    val startingPoint = if (futureMostActivity < today) today else futureMostActivity
    return startingPoint.datesUntil(furthestAwaySyncDayIncl.plusDays(1)).toList().also {
        log.debug { "Result of daysUntil: $it" }
    }
}
