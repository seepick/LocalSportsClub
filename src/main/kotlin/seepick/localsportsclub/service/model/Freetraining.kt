package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.time.LocalTime

class Freetraining(
    val id: Int,
    val venue: SimpleVenue,
    val name: String,
    val category: String,
    val date: LocalDate,
    checkedinTime: LocalTime?,
) {
    var checkedinTime: LocalTime? by mutableStateOf(checkedinTime)
    override fun toString(): String = "Freetraining[id=$id, name=$name, date=$date]"
}
