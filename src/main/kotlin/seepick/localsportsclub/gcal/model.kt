package seepick.localsportsclub.gcal

import seepick.localsportsclub.service.date.DateTimeRange
import java.time.LocalDate


data class GcalDeletion(
    val day: LocalDate,
    val activityOrFreetrainingId: Int,
    val isActivity: Boolean,
)

sealed interface GcalEntry {
    val title: String
    val location: String
    val notes: String
    val isActivity: Boolean
    val activityOrFreetrainingId: Int

    data class GcalActivity(
        override val title: String,
        override val location: String,
        override val notes: String,
        val activityId: Int,
        val dateTimeRange: DateTimeRange,
    ) : GcalEntry {
        override val isActivity = true
        override val activityOrFreetrainingId = activityId
    }

    data class GcalFreetraining(
        override val title: String,
        override val location: String,
        override val notes: String,
        val freetrainingId: Int,
        val date: LocalDate,
    ) : GcalEntry {
        override val isActivity = false
        override val activityOrFreetrainingId = freetrainingId
    }
}
