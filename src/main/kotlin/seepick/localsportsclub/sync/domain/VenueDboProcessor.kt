package seepick.localsportsclub.sync.domain

import lsc.repo.VenueDbo

interface VenueDboProcessor {
    fun process(dbo: VenueDbo): VenueDbo
}

fun VenueDbo.process(processors: List<VenueDboProcessor>): VenueDbo =
    processors.fold(this) { acc, processor ->
        processor.process(acc)
    }

class HtmlEntityCleanerVenueDboProcessor : VenueDboProcessor {
    private val htmlSingleQuote = "&#039;"
    private val htmlAnd = "&amp;"
    override fun process(dbo: VenueDbo): VenueDbo {
        var cleanedDbo = dbo
        cleanedDbo = cleanedDbo.copy(street = cleanedDbo.street.replace(htmlSingleQuote, "'"))
        cleanedDbo = cleanedDbo.copy(description = cleanedDbo.description.replace(htmlAnd, "&"))
        cleanedDbo = cleanedDbo.copy(importantInfo = cleanedDbo.importantInfo?.replace(htmlAnd, "&"))
        return cleanedDbo
    }
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
