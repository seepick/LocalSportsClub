package seepick.localsportsclub.service

import seepick.localsportsclub.persistence.ActivityDbo

interface ActivityDboEnricher {
    fun enrich(dbo: ActivityDbo): ActivityDbo
}

fun ActivityDbo.enrich(enrichers: List<ActivityDboEnricher>): ActivityDbo =
    enrichers.fold(this) { acc, enricher ->
        enricher.enrich(acc)
    }
