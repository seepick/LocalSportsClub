package seepick.localsportsclub.service.model

import java.time.LocalDateTime

data class Activity(
    val id: Int,
    val venue: SimpleVenue,
    val name: String,
    val category: String, // aka disciplines/facilities
    val spotsLeft: Int,
    val from: LocalDateTime,
    val to: LocalDateTime,
)
