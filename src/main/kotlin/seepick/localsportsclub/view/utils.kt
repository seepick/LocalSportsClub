package seepick.localsportsclub.view

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
import seepick.localsportsclub.service.ensureMaxLength
import java.io.File
import javax.swing.JOptionPane

@OptIn(ExperimentalResourceApi::class)
fun readImageBitmapFromClasspath(classpath: String): ImageBitmap =
    openFromClasspath(classpath).readAllBytes().decodeToImageBitmap()

@OptIn(ExperimentalResourceApi::class)
fun readImageBitmapFromFile(file: File): ImageBitmap =
    file.inputStream().readAllBytes().decodeToImageBitmap()


private val log = logger {}

fun ViewModel.executeBackgroundTask(
    doBefore: () -> Unit = {},
    doFinally: () -> Unit = {},
    doTask: suspend () -> Unit,
) {
    viewModelScope.launch {
        withContext(Dispatchers.IO) {
            doBefore()
            try {
                doTask()
            } catch (e: Exception) {
                log.error(e) { "Background task failed!" }
                val message = (e.message ?: "").ensureMaxLength(100)
                JOptionPane.showMessageDialog(
                    null,
                    "${e::class.qualifiedName}:\n$message",
                    "Background Task Failed!",
                    JOptionPane.ERROR_MESSAGE,
                )
            } finally {
                doFinally()
            }
        }
    }
}
