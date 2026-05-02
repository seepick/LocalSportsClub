package seepick.localsportsclub.sync.domain

import lsc.repo.VenueDbo

interface VenueDboProcessor {
    fun process(dbo: VenueDbo): VenueDbo
}

fun VenueDbo.process(processors: List<VenueDboProcessor>): VenueDbo =
    processors.fold(this) { acc, processor ->
        processor.process(acc)
    }

class CategoryVenueDboProcessor : VenueDboProcessor {
    override fun process(dbo: VenueDbo): VenueDbo {
        val categories = dbo.facilities.split(",")
        return if (categories.contains("Modern Self Defence")) {
            dbo.copy(
                facilities = categories
                    .minus("Modern Self Defence")
                    .plus("Modern Self Defense")
                    .joinToString(",")
            )
        } else {
            dbo
        }
    }
}
