package seepick.localsportsclub

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.UscApiDeferred
import com.github.seepick.uscclient.UscConfig
import com.github.seepick.uscclient.UscConnector
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.singles.SinglesService

private val log = logger {}

fun uscClientModule(config: LscConfig) = module {
    single { UscConnector.Companion() } bind UscConnector::class
    single {
        UscApiDeferred(
            configProvider = {
                val singlesService: SinglesService by inject()
                log.info { "Deferred UscApi access; accessing runtime configured credentials." }
                UscConfig(
                    credentials = singlesService.verifiedUscCredentials
                        ?: error("No (verified) USC credentials stored!"),
                    responseLogFolder = config.fileResolver.resolve(DirectoryEntry.ApiLogs),
                    currentYear = config.currentYear,
                )
            },
            onConnected = { api ->
                runBlocking {
                    val singlesService: SinglesService by inject()
                    singlesService.plan = api.fetchMembership().plan
                }
            }
        )
    } bind UscApi::class
}
