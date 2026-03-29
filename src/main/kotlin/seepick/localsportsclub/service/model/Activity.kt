package seepick.localsportsclub.service.model

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.github.seepick.uscclient.plan.Plan
import com.github.seepick.uscclient.shared.DateTimeRange
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.date.prettyFromShorterPrint
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.table.TableItemBgColor
import java.time.LocalDate
import java.time.LocalDateTime

fun <R : Remark> List<R>.findMatchingRating(name: String): RemarkRating? {
    val matching = this.filter { name.contains(it.name, ignoreCase = true) }
    return when (matching.size) {
        0 -> null
        1 -> matching.single()
        else -> {
            matching.maxBy { it.name.length }
        }
    }?.rating
}

class Activity(
    val id: Int,
    override val venue: Venue,
    val name: String,
    override val category: Category, // aka disciplines/facilities
    val dateTimeRange: DateTimeRange,
    override val plan: Plan.UscPlan,
    teacher: String?,
    description: String?,
    spotsLeft: Int,
    state: ActivityState,
    cancellationLimit: LocalDateTime?,
) : HasVenue, HasCategory, HasDistance by venue, HasPlan, TableItemBgColor {

    var state: ActivityState by mutableStateOf(state)
    var teacher: String? by mutableStateOf(teacher)
    var description: String? by mutableStateOf(description)
    var spotsLeft: Int by mutableStateOf(spotsLeft)
    var cancellationLimit: LocalDateTime? by mutableStateOf(cancellationLimit)

    val score: Score? by derivedStateOf { calcScore() }
    //    override val tableBgColor: Color? get() = Lsc.colors.forScore(venue)
    override val tableBgColor: Color? by derivedStateOf { Lsc.colors.forScore(score, venue) }

    // not possible due to mixed setup of table columns (doing logic in view/composable together)
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val nameWithTeacherIfPresent: Flow<String> = snapshotFlow { name to teacher }
//        .mapLatest { (name, teacher) -> if (teacher == null) name else "$name /$teacher" }

    val remarkRating: RemarkRating? by derivedStateOf {
        venue.activityRemarks.findMatchingRating(name)
    }

    val teacherRemarkRating: RemarkRating? by derivedStateOf {
        this.teacher?.let { venue.teacherRemarks.findMatchingRating(it) }
    }

    fun isInPast(today: LocalDate): Boolean =
        dateTimeRange.from.toLocalDate() < today

    fun copy(
        name: String = this.name,
        teacher: String? = this.teacher,
        state: ActivityState = this.state,
        venue: Venue = this.venue,
        dateTimeRange: DateTimeRange = this.dateTimeRange,
        cancellationLimit: LocalDateTime? = this.cancellationLimit,
    ) = Activity(
        id = id,
        venue = venue,
        name = name,
        category = category,
        dateTimeRange = dateTimeRange,
        plan = plan,
        teacher = teacher,
        description = description,
        spotsLeft = spotsLeft,
        state = state,
        cancellationLimit = cancellationLimit,
    )

    override fun toString() =
        "Activity[" +
                "id=$id, " +
                "name=$name, " +
                "venue.slug=${venue.slug}, " +
                "date=${dateTimeRange.prettyFromShorterPrint(0)}, " +
                "state=$state, " +
                "teacher=$teacher" +
                "]"

    companion object {
        fun comparator(today: LocalDate) = Comparator<Activity> { a1, a2 ->
            val a1IsNew = a1.dateTimeRange.from.toLocalDate() >= today
            val a2IsNew = a2.dateTimeRange.from.toLocalDate() >= today
            if (a1IsNew && a2IsNew) {
                a1.dateTimeRange.from.compareTo(a2.dateTimeRange.from)
            } else if (!a1IsNew && !a2IsNew) {
                a2.dateTimeRange.from.compareTo(a1.dateTimeRange.from)
            } else {
                if (a1IsNew) -1 else 1
            }
        }
    }
}

enum class ActivityState(val label: String) {
    // CAVE: names are used for DB mapping!
    Blank("blank"),
    Booked("booked"), // reserved for future (called "scheduled" for freetrainings)
    Checkedin("checked-in"), // past activities i actually attended
    Noshow("no-show"),
    CancelledLate("cancelled late");

    fun iconStringAndSuffix() = when (this) {
        Blank -> ""
        Booked -> "${LscIcons.reservedEmoji} "
        Checkedin -> "${LscIcons.checkedinEmoji} "
        Noshow -> "${LscIcons.noshowEmoji} "
        CancelledLate -> "${LscIcons.cancelledLateEmoji} "
    }
}
