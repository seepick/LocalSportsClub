package seepick.localsportsclub.service.date

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

fun LocalDate.monthRange(): ClosedRange<LocalDate> =
    withDayOfMonth(1)..atEndOfMonth()

fun LocalDate.atEndOfMonth(): LocalDate =
    YearMonth.of(year, month).atEndOfMonth()

fun LocalDate.sameYearMonth(target: LocalDate) =
    year == target.year && month == target.month

fun LocalDateTime.sameYearMonth(target: LocalDate) =
    year == target.year && month == target.month
