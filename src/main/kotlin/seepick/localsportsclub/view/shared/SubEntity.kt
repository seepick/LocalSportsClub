package seepick.localsportsclub.view.shared

import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.HasVenue
import java.time.LocalDate

sealed interface SubEntity : HasVenue {
    val maybeActivity: Activity?
    val maybeFreetraining: Freetraining?
    val id: Int
    val name: String
    fun dateFormatted(year: Int): String
    val category: String
    val date: LocalDate
    val bookLabel: String
    val bookedLabel: String

    data class ActivityEntity(val activity: Activity) : SubEntity, HasVenue by activity {
        override val maybeActivity = activity
        override val maybeFreetraining = null
        override val id = activity.id
        override val name = activity.name
        override fun dateFormatted(year: Int) = activity.dateTimeRange.prettyPrint(year)
        override val category = activity.category
        override val bookLabel = "Book"
        override val bookedLabel = "booked"
        override val date: LocalDate = activity.dateTimeRange.from.toLocalDate()
    }

    data class FreetrainingEntity(val freetraining: Freetraining) : SubEntity, HasVenue by freetraining {
        override val maybeFreetraining = freetraining
        override val maybeActivity = null
        override fun dateFormatted(year: Int) = freetraining.date.prettyPrint(year)
        override val id = freetraining.id
        override val name = freetraining.name
        override val category = freetraining.category
        override val bookLabel = "Schedule"
        override val bookedLabel = "scheduled"
        override val date = freetraining.date
    }
}
