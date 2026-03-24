package seepick.localsportsclub.service

import com.github.seepick.uscclient.activity.ActivityDetails
import seepick.localsportsclub.persistence.ActivityDbo

typealias ActivityDbosWithDetails = List<Pair<ActivityDbo, ActivityDetails>>

interface ActivityEnricher {
    suspend fun enrich(original: ActivityDbosWithDetails): ActivityDbosWithDetails
}
