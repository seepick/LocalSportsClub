package seepick.localsportsclub.service

import seepick.localsportsclub.persistence.ActivityDbo

interface ActivityDboProcessor {
    fun process(dbo: ActivityDbo): ActivityDbo
}

fun ActivityDbo.process(processors: List<ActivityDboProcessor>): ActivityDbo =
    processors.fold(this) { acc, processor ->
        processor.process(acc)
    }
