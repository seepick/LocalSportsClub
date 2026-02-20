package seepick.localsportsclub.service.model

import com.github.seepick.uscclient.plan.Plan

interface HasPlan {
    val plan: Plan.UscPlan
}

interface HasDistance {
    val distanceInKm: Double
}

interface HasSlug {
    val slug: String
}

interface HasVenue {
    val venue: Venue
}
