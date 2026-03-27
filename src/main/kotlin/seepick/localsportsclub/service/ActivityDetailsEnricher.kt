package seepick.localsportsclub.service

import com.github.seepick.uscclient.activity.ActivityDetails
import seepick.localsportsclub.persistence.ActivityDbo

typealias ActivityDbosWithDetails = Map<ActivityDbo, ActivityDetails>

interface ActivityDetailsEnricher {
    suspend fun enrich(original: ActivityDbosWithDetails): ActivityDbosWithDetails
}
