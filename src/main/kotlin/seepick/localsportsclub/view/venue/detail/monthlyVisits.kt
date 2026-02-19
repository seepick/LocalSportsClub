package seepick.localsportsclub.view.venue.detail

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import java.time.LocalDate

@Composable
fun MonthlyVisitsPanel(
    model: MonthlyVisitsModel,
) {
    Text(
        text = "Available visits this month: ${model.totalAvailable} of ${MonthlyVisitsModel.MAX_VISITS}",
        fontSize = 10.sp,
    )
}

data class MonthlyVisitsModel(
    val checkedinThisMonth: Int,
    val bookedThisMonth: Int,
) {
    companion object {
        /*
        S-members kunnen tot 2x per maand bij deze locatie inchecken
        M-members kunnen tot 4x per maand bij deze locatie inchecken
        L & XL-members kunnen tot 6x per maand bij deze locatie inchecken
         */
        const val MAX_VISITS = 6
    }

    val totalUsed = checkedinThisMonth + bookedThisMonth
    val totalAvailable = MAX_VISITS - totalUsed
}

fun List<Activity>.toMonthlyVisitsModel(today: LocalDate): MonthlyVisitsModel {
    val currentMonthsActivities =
        filter { it.dateTimeRange.from.month == today.month && it.dateTimeRange.from.year == today.year }
    return MonthlyVisitsModel(
        checkedinThisMonth = currentMonthsActivities.count { it.state == ActivityState.Checkedin },
        bookedThisMonth = currentMonthsActivities.count { it.state == ActivityState.Booked },
    )
}
