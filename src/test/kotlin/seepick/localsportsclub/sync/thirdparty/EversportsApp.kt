package seepick.localsportsclub.sync.thirdparty

import kotlinx.coroutines.runBlocking
import seepick.localsportsclub.api.NoopResponseStorage
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.httpClient

object EversportsApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val fetcher = EversportsFetcher(
            httpClient = httpClient,
            responseStorage = NoopResponseStorage,
            clock = SystemClock,
        )
        val events = runBlocking {
            fetcher.fetch(HotFlowYogaStudio.Zuid)
            fetcher.fetch(HotFlowYogaStudio.Rivierenbuurt)
            fetcher.fetch(HotFlowYogaStudio.Jordaan)
        }
        println("Got ${events.size} events:")
        events.forEach {
            println("- $it")
        }
    }
}
