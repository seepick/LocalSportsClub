package seepick.localsportsclub.devApp

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.UscApiMock
import com.github.seepick.uscclient.UscConnector
import com.github.seepick.uscclient.UscConnectorMock
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

private val log = KotlinLogging.logger {}

fun mockUscClientModule() = module {
    log.debug { "Wiring mocked USC API." }
    singleOf(::UscApiMock) bind UscApi::class
    single { UscConnectorMock() } bind UscConnector::class // to verify login credentials only
}
