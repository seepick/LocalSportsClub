package seepick.localsportsclub.service

import seepick.localsportsclub.persistence.ActivityDbo

class NameFixingActivityDboProcessor : ActivityDboProcessor {

    private val symbols = setOf('.', '_', '*', ' ')

    override fun process(dbo: ActivityDbo) =
        if (
            symbols.any { symbol ->
                dbo.name.startsWith(symbol) || dbo.name.endsWith(symbol)
            }
        ) {
            dbo.copy(name = dbo.name.trim { it in symbols })
        } else {
            dbo
        }
}
