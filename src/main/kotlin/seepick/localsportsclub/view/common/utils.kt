package seepick.localsportsclub.view.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import seepick.localsportsclub.openFromClasspath
import java.io.File

private val log = logger {}

@OptIn(ExperimentalResourceApi::class)
fun readImageBitmapFromClasspath(classpath: String): ImageBitmap =
    openFromClasspath(classpath).readAllBytes().decodeToImageBitmap()

@OptIn(ExperimentalResourceApi::class)
fun readImageBitmapFromFile(file: File): ImageBitmap = file.inputStream().readAllBytes().decodeToImageBitmap()


fun ViewModel.executeBackgroundTask(
    errorMessage: String,
    doBefore: () -> Unit = {},
    doFinally: () -> Unit = {},
    doTask: suspend () -> Unit,
) {
    viewModelScope.launch {
        withContext(Dispatchers.IO) {
            executeTask(errorMessage, doBefore, doFinally, doTask)
        }
    }
}

fun ViewModel.executeViewTask(
    errorMessage: String,
    doBefore: () -> Unit = {},
    doFinally: () -> Unit = {},
    doTask: suspend () -> Unit,
) {
    viewModelScope.launch {
        executeTask(errorMessage, doBefore, doFinally, doTask)
    }
}

private suspend fun executeTask(
    errorMessage: String,
    doBefore: () -> Unit = {},
    doFinally: () -> Unit = {},
    doTask: suspend () -> Unit,
) {
    log.debug { "Executing background task..." }
    doBefore()
    try {
        doTask()
    } catch (e: Throwable) {
        when (e) {
            is Exception, is NoClassDefFoundError -> {
                log.error(e) { "Background task failed!" }
                showErrorDialog(
                    title = "Background Task Failed!",
                    message = errorMessage,
                    exception = e,
                )
            }

            else -> {
                log.error(e) { "Unhandled error thrown during background task! ($errorMessage)" }
                throw e
            }
        }
    } finally {
        doFinally()
    }
}
