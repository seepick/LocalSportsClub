package seepick.localsportsclub.service

import java.time.LocalDate
import java.time.LocalDateTime

interface Clock {
    fun now(): LocalDateTime
    fun today(): LocalDate
}

object SystemClock : Clock {
    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun today(): LocalDate = LocalDate.now()
}
