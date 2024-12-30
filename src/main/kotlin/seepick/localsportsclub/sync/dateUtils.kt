package seepick.localsportsclub.sync

import seepick.localsportsclub.service.date.Clock
import java.time.LocalDate

fun Clock.daysUntil(syncDaysAhead: Int, futureMostDate: LocalDate?): List<LocalDate> {
    val today = today()
    val furthestAwaySyncDayIncl = today.plusDays(syncDaysAhead.toLong() - 1)
    val futureMostActivity = futureMostDate ?: today.minusDays(1)
    val startingPoint = if (futureMostActivity < today) today else futureMostActivity
    return startingPoint.datesUntil(furthestAwaySyncDayIncl.plusDays(1)).toList()
}
