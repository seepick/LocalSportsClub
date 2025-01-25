package seepick.localsportsclub.service.date

import java.time.Duration
import java.time.LocalDate


fun LocalDate.daysBetween(other: LocalDate): Long =
    Duration.between(atStartOfDay(), other.atStartOfDay()).toDays()
