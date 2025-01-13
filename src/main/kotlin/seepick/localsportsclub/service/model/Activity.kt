package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.shared.HasVenue

enum class ActivityState {
    // CAVE: names are used for DB mapping!
    Blank, Booked, Checkedin, Noshow;

    fun iconStringAndSuffix() = when (this) {
        Blank -> ""
        Booked -> "${LscIcons.booked} "
        Checkedin -> "${LscIcons.checkedin} "
        Noshow -> "${LscIcons.noshow} "
    }
}

class Activity(
    val id: Int,
    override val venue: Venue,
    val name: String,
    val category: String, // aka disciplines/facilities
    val dateTimeRange: DateTimeRange,
    teacher: String?,
    spotsLeft: Int,
    state: ActivityState,
) : HasVenue {

//    val nameWithTeacherIfPresent =
//        if (teacher == null) name else "$name /$teacher"
    // not possible due to mixed setup of table columns (doing logic in view/composable together)
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val nameWithTeacherIfPresent: Flow<String> = snapshotFlow { name to teacher }
//        .mapLatest { (name, teacher) -> if (teacher == null) name else "$name /$teacher" }

    var state: ActivityState by mutableStateOf(state)
    var teacher: String? by mutableStateOf(teacher)
    var spotsLeft: Int by mutableStateOf(spotsLeft)

    override fun toString() = "Activity[id=$id, name=$name, teacher=$teacher, venue.slug=${venue.slug}]"
}
