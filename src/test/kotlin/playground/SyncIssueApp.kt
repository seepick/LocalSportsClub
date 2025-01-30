package playground

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.qos.logback.classic.Level
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import seepick.localsportsclub.persistence.SinglesTable
import seepick.localsportsclub.persistence.connectToDatabaseEnvAware
import seepick.localsportsclub.reconfigureLog
import seepick.localsportsclub.service.workParallel
import seepick.localsportsclub.view.common.launchBackgroundTask

private class ExitHandler {
    private val log = logger {}
    fun onExit() {
        log.debug { "onExit() opening transaction" }
        transaction {
            log.debug { "In transaction" }
            SinglesTable.selectAll().toList()
        }
    }
}

private class MySyncer {
    private val log = logger {}
    suspend fun sync() {
        log.debug { "sync() START" }
        newSuspendedTransaction {
//        transaction {
//        runBlocking { // !!! this one here is/was the problem !!!
            coroutineScope {
                SinglesTable.selectAll().toList()
                workParallel(5, (1..100).toList()) { i ->
                    log.debug { "executing step $i/100" }
                    println("isActive: $isActive")
                    workload()
                }
            }
//        }
        }
        log.debug { "sync() DONE" }
    }

    private fun workload() {
        fib(42)
    }
}

private fun fib(n: Int): Long {
    return if (n == 0) 0
    else if (n == 1) 1
    else fib(n - 1) + fib(n - 2)
}

private class MyViewModel(private val syncer: MySyncer) : ViewModel() {
    private val log = logger {}
    private var job: Job? = null
    fun onStartSync() {
        log.debug { "onStartSync()" }
        job = launchBackgroundTask("Synchronisation of data failed!") {
            syncer.sync()
        }
    }

    fun onStopSync() {
        log.debug { "onStopSync() job=$job" }
        viewModelScope.launch {
            log.debug { "sync cancel&join ..." }
            job?.cancelAndJoin()
            log.debug { "sync cancel&join ... DONE" }
        }
    }
}

object SyncIssueApp {

    @JvmStatic
    fun main(args: Array<String>) {
        reconfigureLog(false, mapOf("playground" to Level.DEBUG))
        application {
            KoinApplication(application = {
                modules(module {
                    singleOf(::ExitHandler)
                    singleOf(::MySyncer)
                    viewModelOf(::MyViewModel)
                })
            }) {
                connectToDatabaseEnvAware(isProd = false)
                val exitHandler: ExitHandler = koinInject()
                val viewModel: MyViewModel = koinInject()
                Window(
                    state = rememberWindowState(),
                    onCloseRequest = {
                        exitHandler.onExit()
                        exitApplication()
                    },
                ) {
                    Column {
                        Button(onClick = {
                            viewModel.onStartSync()
                        }) {
                            Text("Start Sync")
                        }
                        Button(onClick = {
                            println("before stop sync button")
                            viewModel.onStopSync()
                            println("after stop sync button")
                        }) {
                            Text("Stop Sync")
                        }
                    }
                }
            }
        }
    }
}

object ParentChildContext {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val parent = GlobalScope.launch {
                launch {
                    println("Child 1 runs...")
                    delay(500)
                    println("Child 1 runs... DONE")
                }
                launch(Job()) { // won't be cancelled
                    println("Child 2 runs...")
                    delay(500)
                    println("Child 2 runs... DONE")
                }
            }
            delay(100)
            parent.cancel()
            delay(500)
            println("END")
        }

    }
}
