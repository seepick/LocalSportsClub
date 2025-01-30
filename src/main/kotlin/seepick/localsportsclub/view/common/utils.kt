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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

fun ViewModel.launchBackgroundTask(
    errorMessage: String,
    doBefore: () -> Unit = {},
    doFinally: () -> Unit = {},
    doTask: suspend () -> Unit,
): Job = executeTask(Dispatchers.IO, errorMessage, doBefore, doFinally, doTask)

fun ViewModel.launchViewTask(
    errorMessage: String,
    doBefore: () -> Unit = {},
    doFinally: () -> Unit = {},
    doTask: suspend () -> Unit,
): Job = executeTask(Dispatchers.Main, errorMessage, doBefore, doFinally, doTask)

private fun ViewModel.executeTask(
    dispatcher: CoroutineDispatcher,
    errorMessage: String,
    doBefore: () -> Unit = {},
    doFinally: () -> Unit = {},
    doTask: suspend () -> Unit,
): Job =
    viewModelScope.launch(dispatcher + exceptionHandler(errorMessage)) {
        log.debug { "Executing task..." }
        doBefore()
        try {
            doTask()
        } finally {
            doFinally()
        }
    }

private fun exceptionHandler(errorMessage: String) = CoroutineExceptionHandler { _, throwable ->
    when (throwable) {
        is Exception, is NoClassDefFoundError -> {
            log.error(throwable) { "Executing task failed!" }
            showErrorDialog(
                message = errorMessage,
                exception = throwable,
            )
        }

        else -> { // let application crash on Error
            log.error(throwable) { "Unhandled error thrown during task! ($errorMessage)" }
            throw throwable
        }
    }
}

fun Modifier.bottomBorder(strokeWidth: Dp, color: Color) = composed(factory = {
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
})

fun Modifier.applyTestTag(testTagName: String?) = let { if (testTagName == null) it else it.testTag(testTagName) }
