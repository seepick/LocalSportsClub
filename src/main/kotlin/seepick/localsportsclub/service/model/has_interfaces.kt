package seepick.localsportsclub.service.model

import com.github.seepick.uscclient.plan.Plan
import kotlin.math.roundToInt

interface HasPlan {
    val plan: Plan.UscPlan
}

interface HasDistance {
    val distanceInKm: Double
    val distanceFormatted: String
        get() = if (distanceInKm < MAX_DISTANCE_KM) {
            String.format("%.1f", distanceInKm)
        } else {
            distanceInKm.roundToInt().toString()
        }

    companion object {
        const val MAX_DISTANCE_KM = 5.0
    }
}

interface HasVenue {
    val venue: Venue
}

interface HasCategory {
    val category: Category
}
