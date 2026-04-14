package seepick.localsportsclub.service

import lsc.repo.ActivityDbo

interface ActivityDboProcessor {
    fun process(dbo: ActivityDbo): ActivityDbo
}

fun ActivityDbo.process(processors: List<ActivityDboProcessor>): ActivityDbo =
    processors.fold(this) { acc, processor ->
        processor.process(acc)
    }
