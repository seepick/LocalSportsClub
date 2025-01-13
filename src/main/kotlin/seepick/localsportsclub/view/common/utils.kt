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

@OptIn(ExperimentalResourceApi::class)
fun readImageBitmapFromClasspath(classpath: String): ImageBitmap =
    openFromClasspath(classpath).readAllBytes().decodeToImageBitmap()

@OptIn(ExperimentalResourceApi::class)
fun readImageBitmapFromFile(file: File): ImageBitmap = file.inputStream().readAllBytes().decodeToImageBitmap()


private val log = logger {}

fun ViewModel.executeBackgroundTask(
    errorMessage: String,
    doBefore: () -> Unit = {},
    doFinally: () -> Unit = {},
    doTask: suspend () -> Unit,
) {
    viewModelScope.launch {
        withContext(Dispatchers.IO) {
            log.debug { "Executing background task..." }
            doBefore()
            try {
                doTask()
            } catch (e: Exception) {
                log.error(e) { "Background task failed!" }
                showErrorDialog(
                    title = "Background Task Failed!",
                    message = errorMessage,
                    exception = e,
                )
            } finally {
                doFinally()
            }
        }
    }
}
