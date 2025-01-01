package seepick.localsportsclub.service.date

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import java.time.LocalTime

fun Arb.Companion.dateTimeRange() = arbitrary {
    val from = localDateTime().next()
    DateTimeRange(
        from = from,
        to = from.plusMinutes(long(min = 30, max = 120).next()),
    )
}

fun Arb.Companion.timeRange() = arbitrary {
    val start = LocalTime.of(int(min = 0, max = 20).next(), int(min = 0, max = 59).next())
    TimeRange(
        start = start,
        end = start.plusMinutes(long(min = 10, max = 180).next())
    )
}
