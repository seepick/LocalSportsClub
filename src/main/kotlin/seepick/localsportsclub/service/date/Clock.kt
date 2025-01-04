package seepick.localsportsclub.service.date

import java.time.LocalDate
import java.time.LocalDateTime

interface Clock {
    fun now(): LocalDateTime
    fun today(): LocalDate
}

object SystemClock : Clock {
    override fun now(): LocalDateTime = LocalDateTime.now().withNano(0)
    override fun today(): LocalDate = LocalDate.now()
}
