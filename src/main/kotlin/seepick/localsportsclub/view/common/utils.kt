package seepick.localsportsclub.view.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
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
    log.debug { "Executing task..." }
    doBefore()
    try {
        doTask()
    } catch (e: Throwable) {
        when (e) {
            is Exception, is NoClassDefFoundError -> {
                log.error(e) { "Executing task failed!" }
                showErrorDialog(
                    message = errorMessage,
                    exception = e,
                )
            }

            else -> {
                log.error(e) { "Unhandled error thrown during task! ($errorMessage)" }
                throw e
            }
        }
    } finally {
        doFinally()
    }
}

fun Modifier.bottomBorder(strokeWidth: Dp, color: Color) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

fun Modifier.applyTestTag(testTagName: String?) =
    let { if (testTagName == null) it else it.testTag(testTagName) }
